package com.demo.petstory.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.petstory.adapter.MyAdapter;
import com.demo.petstory.R;
import com.demo.petstory.model.Chat;
import com.demo.petstory.util.sub_ItemDecoration;
import com.demo.petstory.util.util;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

/**
 * 채팅창
 **/
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatMain";
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private FirebaseDatabase database;
    private EditText etText;
    private Button btnSend, picture;
    private String stEmail, nickName, my;
    private String nn[];
    private ArrayList<Chat> chatArrayList;
    private MyAdapter mAdapter;
    private TextView topNick;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseUser user;

    private String userToken;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        LinearLayout customActionBar = new LinearLayout(this);
        customActionBar.setOrientation(LinearLayout.HORIZONTAL);

        ImageView imageView = new ImageView(this);
        imageView.setScaleY(0.9f);
        imageView.setScaleX(0.9f);
        imageView.setImageResource(R.drawable.ic_pet_white);
        TextView titleTextView = new TextView(this);
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

        setContentView(R.layout.chatroom);

        Intent intent = getIntent();
        nickName = intent.getStringExtra("nickName");
        my = intent.getStringExtra("my");
        topNick = findViewById(R.id.guest);
        topNick.setText(nickName + "님과 대화중");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        chatArrayList = new ArrayList<>();
        stEmail = user.getEmail();

        Log.e(TAG, "stEmail : " + stEmail);

        nn = new String[2];

        btnSend = findViewById(R.id.btn_send);
        etText = findViewById(R.id.chat);

        recyclerView = findViewById(R.id.room_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MyAdapter(chatArrayList, stEmail, nickName);
        recyclerView.addItemDecoration(new sub_ItemDecoration(getApplicationContext(), 10));
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();

        recyclerView.setAdapter(mAdapter);

        etText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        // 검색 동작
                        break;
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });

        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.get("nickName").toString().equals(nickName)) {

                                    ChildEventListener childEventListener = new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                                            Chat chat = dataSnapshot.getValue(Chat.class);

                                            String time = dataSnapshot.getKey().toString();
                                            chat.setDate(time);
                                            chatArrayList.add(chat);

                                            mAdapter.notifyDataSetChanged();
                                            recyclerView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                                }
                                            });
                                        }

                                        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                                        }

                                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                                        }

                                        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                                        }

                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    };

                                    nn[0] = user.getUid();
                                    nn[1] = document.getId();
                                    Arrays.sort(nn);

                                    DatabaseReference ref = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message");
                                    ref.addChildEventListener(childEventListener);

                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etText.getText().toString().length() > 0) {
                    final String stText = etText.getText().toString();

//                    util.snackbar(ChatActivity.this, "MSG : " + stText, Snackbar.LENGTH_SHORT);
//                    Toast.makeText(ChatActivity.this, "MSG : " + stText, Toast.LENGTH_SHORT).show();
                    etText.getText().clear();

                    etText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etText.getWindowToken(), 0);

                    database = FirebaseDatabase.getInstance();

                    db.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            if (document.get("nickName").toString().equals(nickName)) {

                                                Calendar c = Calendar.getInstance();
                                                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                                String datetime = dateformat.format(c.getTime());

                                                nn[0] = user.getUid();
                                                nn[1] = document.getId();
                                                Arrays.sort(nn);

                                                //DatabaseReference myRef = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message").child(datetime);
                                                DatabaseReference myRef = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message").child(datetime);
                                                DatabaseReference dr = database.getReference("chat").child(document.getId() + "&" + user.getUid()).child("message").child(datetime);
                                                Hashtable<String, String> numbers
                                                        = new Hashtable<String, String>();
                                                numbers.put("email", stEmail);
                                                numbers.put("text", stText);
                                                myRef.setValue(numbers);
                                                dr.setValue(numbers);

                                                sendPush(userToken, nickName, stText);


                                            }
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                }
                            });

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    });

                }
            }
        });


        picture = findViewById(R.id.picture);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ImageChoicePopupActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        getToken();
    }

    String[] sImg;
    String[] uri;
    ImageView iv;
    String ca;


    @SuppressLint("StaticFieldLeak")
    private void sendPush(final String token, final String id, final String text) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parms) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("body", text);
                    dataJson.put("title", id);
                    json.put("notification", dataJson);
                    json.put("to", token);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + "AAAA0oHZgoo:APA91bFyR0bxsMG6pSWGTCjBG382_09wYuf224xyKDfbWGrrVJa9EE2vMxSzIMLVupGfwDXl8NH9dM1ImY9W-vHJSZPnzwz9vbQUDNOYiBYVIqTxsfyjmvr7TB8jsUYbnjy6O-NAuRk8")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                } catch (Exception e) {
                    Log.d("error", e + "");
                }
                return null;
            }
        }.execute();
    }

    private void getToken() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("nickName", nickName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 해당 사용자의 토큰을 가져옵니다.
                                userToken = document.getString("fcmToken");
                                Log.d(TAG, "userToken : " + userToken);

                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void saveImage(String uri) {

        final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage("이미지 전송중...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();
        final UploadTask[] uploadTask = new UploadTask[1];
        String url;
        // 현재 시간 가져오기
        Date currentTime = new Date();

        // 시간을 특정 포맷으로 변환
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formattedTime = formatter.format(currentTime);

        // 결과 출력
        Log.d("CurrentTime", "현재 시간: " + formattedTime);

        final Uri file;
        file = Uri.fromFile(new File(uri));
        StorageReference riversRef = storageRef.child("chat/" + "postImg_" + formattedTime);
        uploadTask[0] = riversRef.putFile(file);

        uploadTask[0].addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final StorageReference ref = storageRef.child("chat/" + "postImg_" + formattedTime);
                uploadTask[0] = ref.putFile(file);

                Task<Uri> urlTask = uploadTask[0].continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            final Uri downloadUri = task.getResult();
                            Log.d(TAG, "downloadUri: " + downloadUri);

                            FirebaseStorage storage = FirebaseStorage.getInstance("gs://petstory-f9843.appspot.com");
                            final StorageReference storageRef = storage.getReference();

                            database = FirebaseDatabase.getInstance();

                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd k:mm:ss");
                            String datetime = dateformat.format(c.getTime());

                            db.collection("users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                                    if (document.get("nickName").toString().equals(nickName)) {

                                                        Calendar c = Calendar.getInstance();
                                                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                                                        String datetime = dateformat.format(c.getTime());

                                                        nn[0] = user.getUid();
                                                        nn[1] = document.getId();
                                                        Arrays.sort(nn);

                                                        //DatabaseReference myRef = database.getReference("friend").child(user.getUid()).child(document.getId()).child("message").child(datetime);
                                                        DatabaseReference myRef = database.getReference("chat").child(user.getUid() + "&" + document.getId()).child("message").child(datetime);
                                                        DatabaseReference dr = database.getReference("chat").child(document.getId() + "&" + user.getUid()).child("message").child(datetime);
                                                        Hashtable<String, String> numbers
                                                                = new Hashtable<String, String>();
                                                        numbers.put("email", stEmail);
                                                        numbers.put("image", downloadUri.toString());
                                                        myRef.setValue(numbers);
                                                        dr.setValue(numbers);

                                                    }
                                                }
                                            } else {
                                                Log.w(TAG, "Error getting documents.", task.getException());
                                            }
                                        }
                                    });
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                }
                            });

                        }
                    }
                });
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "requestCode ->" + requestCode);
        if (requestCode == 0) {
            ca = data.getStringExtra("postImgPath");
            saveImage(ca);
        } else if (resultCode == 2) {
            ca = data.getStringExtra("camera");
            saveImage(ca);
        }
    }
}


