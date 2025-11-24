package com.demo.petstory.activity;

import static com.demo.petstory.util.util.nickName;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.demo.petstory.R;
import com.demo.petstory.fragment.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**************************************************************************************************************************

 @ author 양준혁.
 @ since 2020.12.20 ~
 * -------------------------------------------------------------------------------------------------------------------- *
 *   작성일자      작 성 자     버 전              작    성      내    용
 * -----------   ---------  --------  --------------------------------------------------------------------------------- *
 * 2020.12.20      양준혁	 1.0     펫 스토리 구현 및 테스트 완료
 * 2021.01.10      양준혁     1.0     DeepLink,FCM 기능 추가
 * 2024.12.31      양준혁	 1.0	 - 리소스 정리 및 AGP 최신화
 *                                   - 깃 허브 소스 코드 업로드
 ************************************************************************************************************************/

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String password = "";
    private TextView toolbarNickName;
    private ImageView user_icon;
    private BottomNavigationView bottomNavigationView;
    private FragmentMain fragmentMain;
    private FragmentSub fragmentSub;
    private FragmentNewPost fragmentNewPost;
    private FragmentMy fragmentMy;
    private FragmentContentMain fragmentContentMain;
    private String fullString;
    private FragmentManager fragmentManager;
    private Menu menu;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DrawerLayout drawerLayout;
    private View drawerView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        LinearLayout customActionBar = new LinearLayout(this);
        customActionBar.setOrientation(LinearLayout.HORIZONTAL);

        AppbarSetting(customActionBar);
        setContentView(R.layout.activity_main);
        Intent fcm = new Intent(getApplicationContext(), FirebaseMessagingService.class);
        startService(fcm);

        getSupportActionBar().hide();
        getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        Intent intent = getIntent();
        if (intent != null) {
            String notificationData = intent.getStringExtra("FCM_PetDiary");
            if (notificationData != null)
                Log.d("FCM_PetDiary", notificationData);
            Log.d("FCM_PetDiary", FirebaseMessaging.getInstance().toString());
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawerView);
        drawerLayout.setDrawerListener(listener);
        toolbarNickName = findViewById(R.id.toolbar_nickName);
        user_icon = findViewById(R.id.user_icon);

        Uri data = intent.getData();

        if (data != null)
            Log.d(TAG, "data ->" + data);
        /*딥링크로 받아오는 부분 */
        deepLink();

        if (user == null) {
            /* 로그인 페이지로 이동 */
            myStartActivity(LoginActivity.class);
            finish();
        } else {
            /* 자동로그인 확인 */
            checkPassword();
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        if (user == null) {
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> map = new HashMap<>();
                        map.put("fcmToken", token);
                        db.collection("users").document(user.getUid()).update(map);
                        Log.d(TAG, "token -> " + token);
                    }
                });
    }

    private void deepLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d(TAG, "deeplink -> " + deepLink.toString());

                            fullString = deepLink.toString();
                            Log.d(TAG, "uid ->" + fullString);

                            Handler mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    if (fullString != null) {
                                        String[] splitText = fullString.split("/");
                                        fragmentSub.getPostID(splitText[3]);
                                    }
                                }
                            }, 3000);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void AppbarSetting(LinearLayout customActionBar) {
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setScaleY(0.9f);
        imageView.setScaleX(0.9f);
        imageView.setImageResource(R.drawable.ic_pet_white);
        TextView titleTextView = new TextView(getApplicationContext());
        titleTextView.setTextSize(17);
        titleTextView.setText("Pet Story");
        titleTextView.setTextColor(Color.parseColor("#ffffff"));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMarginStart(30); // start 마진 설정

        customActionBar.addView(imageView);
        customActionBar.addView(titleTextView, layoutParams);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(customActionBar);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void blank(View view) {
    }

    public void bookMarkOnClick(View view) {
        myStartActivity2(SettingBookMarkActivity.class);
    }

    public void blockFriendOnClick(View view) {
        myStartActivity2(SettingBlockFriendsActivity.class);
    }

    public void noticeOnClick(View view) {
        myStartActivity2(SettingNotificationActivity.class);
    }

    public void passwordSetOnClick(View view) {
        myStartActivity2(LoginConfirmActivity.class, "setPassword");
    }

    public void customerCenterOnClick(View view) {
        myStartActivity2(SettingCustomerActivity.class);
    }

    public void logOutOnClick(View view) {
        startPopupActivity();
    }

    public void unRegisterOnClick(View view) {
        myStartActivity2(LoginConfirmActivity.class, "out");
    }

    public void AppInfoOnClick(View view) {
        myStartActivity2(SettingAppInfoActivity.class);
    }

    private void startPopupActivity() {
        Intent intent = new Intent(getApplicationContext(), LogoutPopupActivity.class);
        startActivityForResult(intent, 0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(drawerView)) {
                    drawerLayout.closeDrawer(drawerView);
                } else {
                    drawerLayout.openDrawer(drawerView);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setFirst() {
        fragmentManager = getSupportFragmentManager();

        fragmentMain = new FragmentMain();
        fragmentManager.beginTransaction().replace(R.id.main_layout, fragmentMain).commit();

        if (fragmentSub == null) {
            fragmentSub = new FragmentSub();
            fragmentManager.beginTransaction().add(R.id.main_layout, fragmentSub).commit();
        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.tab2).setChecked(false);
        menu.findItem(R.id.tab1).setChecked(true);

        fragmentManager.beginTransaction().hide(fragmentSub).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;

            switch (menuItem.getItemId()) {
                case R.id.tab1:
                    if (fragmentMain == null) {
                        fragmentMain = new FragmentMain();
                        fragmentManager.beginTransaction().add(R.id.main_layout, fragmentMain).commit();
                    }
                    selectedFragment = fragmentMain;
                    break;
                case R.id.tab2:
                    if (fragmentSub == null) {
                        fragmentSub = new FragmentSub();
                        fragmentManager.beginTransaction().add(R.id.main_layout, fragmentSub).commit();
                    }
                    selectedFragment = fragmentSub;
                    break;
                case R.id.tab3:
                    if (fragmentNewPost == null) {
                        fragmentNewPost = new FragmentNewPost();
                        fragmentManager.beginTransaction().add(R.id.main_layout, fragmentNewPost).commit();
                    }
                    selectedFragment = fragmentNewPost;
                    break;
                case R.id.tab4:
                    if (fragmentMy == null) {
                        fragmentMy = new FragmentMy();
                        fragmentManager.beginTransaction().add(R.id.main_layout, fragmentMy).commit();
                    }
                    selectedFragment = fragmentMy;
                    break;
                case R.id.tab5:
                    if (fragmentContentMain == null) {
                        fragmentContentMain = new FragmentContentMain();
                        fragmentManager.beginTransaction().add(R.id.main_layout, fragmentContentMain).commit();
                    }
                    selectedFragment = fragmentContentMain;
                    break;
                default:
                    return false;
            }

            showOnlyFragment(selectedFragment);
            return true;
        });
    }


    private void showOnlyFragment(Fragment fragmentToShow) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        List<Fragment> fragments = Arrays.asList(
                fragmentMain, fragmentSub, fragmentNewPost, fragmentMy, fragmentContentMain
        );

        for (Fragment fragment : fragments) {
            if (fragment != null) {
                if (fragment == fragmentToShow) {
                    transaction.show(fragment);
                } else {
                    transaction.hide(fragment);
                }
            }
        }

        transaction.commit();
    }

    private void replaceTo(Fragment showFragment, boolean check, boolean removeNewPost) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (removeNewPost && fragmentNewPost != null) {
            transaction.remove(fragmentNewPost);
            fragmentNewPost = null;
        }

        List<Fragment> allFragments = Arrays.asList(
                fragmentMain, fragmentSub, fragmentNewPost, fragmentMy, fragmentContentMain
        );

        for (Fragment fragment : allFragments) {
            if (fragment == null) continue;

            if (fragment == showFragment) {
                transaction.show(fragment);

                if (fragment == fragmentMain && check) {
                    fragmentMain.setInfo();
                } else if (fragment == fragmentSub) {
                    fragmentSub.refresh();
                } else if (fragment == fragmentMy && check) {
                    fragmentMy.myPageRefresh();
                }

            } else {
                transaction.hide(fragment);

                if (fragment == fragmentMain && check) {
                    fragmentMain.setInfo();
                } else if (fragment == fragmentSub) {
                    fragmentSub.refresh();
                } else if (fragment == fragmentMy && check) {
                    fragmentMy.myPageRefresh();
                }
            }
        }

        transaction.commit();
    }

    public void MainReplaceFragment(boolean check) {
        replaceTo(fragmentMain, check, true);
    }

    public void SubReplaceFragment(boolean check) {
        replaceTo(fragmentSub, check, false);
    }

    public void New_replaceFragment(boolean check) {
        replaceTo(fragmentNewPost, check, false);
    }

    public void contain_replaceFragment(boolean check) {
        replaceTo(fragmentContentMain, check, false);
    }

    public void My_replaceFragment(boolean check) {
        replaceTo(fragmentMy, check, true);
    }




    public static boolean isValidPassword(String password) {
        boolean err = false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if (m.matches()) {
            err = true;
        }
        return err;
    }

    private void myStartActivity2(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    private void myStartActivity2(Class c, String s) {
        Intent intent = new Intent(this, c);
        intent.putExtra("setting", s);
        startActivity(intent);
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void checkPassword() {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //Log.d("@@@", FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
                    if (document != null) {
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            password = document.getData().get("password").toString();
                            toolbarNickName.setText(document.getData().get("nickName").toString() + " 님");
                            nickName = toolbarNickName.getText().toString();
                            if (document.getData().get("profileImg").toString().length() > 0) {
                                setProfileImg(document.getData().get("profileImg").toString());
                            }
                            if (isValidPassword(password)) {
                                setFirst();
                            } else {
                                myStartActivity(SetPasswordActivity.class);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setProfileImg(String profileImg) {
        Glide.with(this).load(profileImg).centerCrop().override(500).into(user_icon);
    }

    private long backKeyPressedTime = 0;
    private Toast toast;

    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerView)) {
            drawerLayout.closeDrawer(drawerView);
        } else {

            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                toast.cancel();
            }
        }
    }

    public void updateMainContent() {
        fragmentMain.setInfo();
    }

    public void updateContent() {
        if (fragmentNewPost != null) {
            fragmentManager.beginTransaction().remove(fragmentNewPost).commit();
            fragmentNewPost = null;
        }
        if (fragmentMain != null) {
            fragmentManager.beginTransaction().show(fragmentMain).commit();
        }
        if (fragmentSub != null) {
            fragmentManager.beginTransaction().hide(fragmentSub).commit();
            fragmentSub.refresh();
        }
        if (fragmentNewPost != null) {
            fragmentManager.beginTransaction().hide(fragmentNewPost).commit();
        }
        if (fragmentMy != null) {
            fragmentManager.beginTransaction().hide(fragmentMy).commit();
            fragmentMy.myPageRefresh();
        }
        if (fragmentContentMain != null) {
            fragmentManager.beginTransaction().hide(fragmentContentMain).commit();
        }
    }

    public void refresh(boolean check) {
        Log.d(TAG, "refresh: " + menu.findItem(R.id.tab2).isChecked());

        if (menu.findItem(R.id.tab1).isChecked()) {
            MainReplaceFragment(check);
        } else if (menu.findItem(R.id.tab2).isChecked()) {
            SubReplaceFragment(check);
        } else if (menu.findItem(R.id.tab3).isChecked()) {
            New_replaceFragment(check);
        } else if (menu.findItem(R.id.tab4).isChecked()) {
            My_replaceFragment(check);
        } else if (menu.findItem(R.id.tab5).isChecked()) {
            contain_replaceFragment(check);
        }

    }
}