package com.demo.petstory.fragment;

import static com.demo.petstory.util.util.nickName;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demo.petstory.activity.Expand_contentsView;
import com.demo.petstory.activity.MainActivity;
import com.demo.petstory.activity.SettingBlockFriendsActivity;
import com.demo.petstory.activity.SettingBookMarkActivity;
import com.demo.petstory.adapter.CustomAdapter;
import com.demo.petstory.model.Data;
import com.demo.petstory.R;
import com.demo.petstory.util.calbacklistener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class FragmentMain extends Fragment implements calbacklistener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Data> arrayList;
    private View view;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final ArrayList<String> bookmark = new ArrayList<String>();
    private final ArrayList<String> like = new ArrayList<String>();
    private DatabaseReference mDatabase;
    BottomNavigationView bottomNavigationView;
    boolean contentCheck;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        moveTop();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();

        setInfo();

        adapter = new CustomAdapter(arrayList, getContext(), this, (MainActivity) getActivity());
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
        SettingBookMarkActivity.setlistener(this);
        SettingBlockFriendsActivity.setlistener(this);

        // 새로 고침
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setInfo();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            moveTop();
        }
    }

    private void moveTop() {
        bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.tab1) {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }



    public void setInfo() {

        Log.e("DEBUG_SETINFO", "setInfo() called");

        try {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e("DEBUG_SETINFO", "FirebaseUser is null");
                return;
            }

            final String uid = user.getUid();
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            final ArrayList<String> mainSource = new ArrayList<>();

            Log.e("DEBUG_SETINFO", "uid -> " + uid);

            arrayList.clear();
            bookmark.clear();
            like.clear();
            mainSource.clear();
            mainSource.add(uid);

            FirebaseApp app = FirebaseApp.getInstance();

            // Realtime DB 레퍼런스 확인
            mDatabase = FirebaseDatabase.getInstance().getReference("friend/" + uid);
            Log.e("DEBUG_SETINFO", "mDatabase ref -> " + mDatabase.toString());
            Log.e("DEBUG_SETINFO", "firebaseApp dbUrl (options) -> " + app.getOptions().getDatabaseUrl());

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("DEBUG_SETINFO", "onDataChange() called, childrenCount = " + snapshot.getChildrenCount());

                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Log.e("DEBUG_SETINFO", "friend child key -> " + postSnapshot.getKey());
                        mainSource.add(postSnapshot.getKey());
                    }

                    Log.e("DEBUG_SETINFO", "mainSource size after friend load = " + mainSource.size());

                    // bookmark 컬렉션 조회
                    Log.e("DEBUG_SETINFO", "Firestore: get bookmark start");
                    db.collection("user-checked/" + uid + "/bookmark")
                            .get()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("DEBUG_SETINFO", "bookmark onFailure -> " + e.getMessage(), e);
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    Log.e("DEBUG_SETINFO", "bookmark onComplete, isSuccessful -> " + task.isSuccessful());

                                    if (task.isSuccessful()) {
                                        if (task.getResult() != null) {
                                            Log.e("DEBUG_SETINFO", "bookmark result size -> " + task.getResult().size());
                                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                                Object postIdObj = document.getData().get("postID");
                                                if (postIdObj != null) {
                                                    String postId = postIdObj.toString();
                                                    bookmark.add(postId);
                                                    Log.e("DEBUG_SETINFO", "bookmark add -> " + postId);
                                                } else {
                                                    Log.e("DEBUG_SETINFO", "bookmark doc has no postID, id -> " + document.getId());
                                                }
                                            }
                                        } else {
                                            Log.e("DEBUG_SETINFO", "bookmark task.getResult() is null");
                                        }

                                        Log.e("DEBUG_SETINFO", "Firestore: get like start");
                                        db.collection("user-checked/" + uid + "/like")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        Log.e("DEBUG_SETINFO", "like onComplete, isSuccessful -> " + task.isSuccessful());

                                                        if (task.isSuccessful()) {
                                                            if (task.getResult() != null) {
                                                                Log.e("DEBUG_SETINFO", "like result size -> " + task.getResult().size());
                                                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                                                    Object postIdObj = document.getData().get("postID");
                                                                    if (postIdObj != null) {
                                                                        String postId = postIdObj.toString();
                                                                        like.add(postId);
                                                                        Log.e("DEBUG_SETINFO", "like add -> " + postId);
                                                                    } else {
                                                                        Log.e("DEBUG_SETINFO", "like doc has no postID, id -> " + document.getId());
                                                                    }
                                                                }
                                                            } else {
                                                                Log.e("DEBUG_SETINFO", "like task.getResult() is null");
                                                            }

                                                            Log.e("DEBUG_SETINFO", "Firestore: get post start");
                                                            db.collection("post")
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            Log.e("DEBUG_SETINFO", "post onComplete, isSuccessful -> " + task.isSuccessful());

                                                                            if (task.isSuccessful()) {
                                                                                if (task.getResult() != null) {
                                                                                    Log.e("DEBUG_SETINFO", "post result size -> " + task.getResult().size());
                                                                                    for (final QueryDocumentSnapshot document : task.getResult()) {
                                                                                        final Data dataList = new Data();

                                                                                        Object uidObj = document.getData().get("uid");
                                                                                        if (uidObj == null) {
                                                                                            Log.e("DEBUG_SETINFO", "post doc has no uid, id -> " + document.getId());
                                                                                            continue;
                                                                                        }

                                                                                        String postUid = uidObj.toString();

                                                                                        for (int j = 0; j < mainSource.size(); j++) {
                                                                                            if (mainSource.get(j).equals(postUid)) {

                                                                                                // bookmark 설정
                                                                                                dataList.setBookmark(false);
                                                                                                for (int i = 0; i < bookmark.size(); i++) {
                                                                                                    if (bookmark.get(i).equals(document.getId())) {
                                                                                                        dataList.setBookmark(true);
                                                                                                        break;
                                                                                                    }
                                                                                                }

                                                                                                // like 설정
                                                                                                dataList.setLike(false);
                                                                                                for (int i = 0; i < like.size(); i++) {
                                                                                                    if (like.get(i).equals(document.getId())) {
                                                                                                        dataList.setLike(true);
                                                                                                        break;
                                                                                                    }
                                                                                                }

                                                                                                // 필드 세팅할 때 null 방어
                                                                                                dataList.setPostID(document.getId());
                                                                                                dataList.setUid(postUid);

                                                                                                Object contentObj = document.getData().get("content");
                                                                                                dataList.setContent(contentObj != null ? contentObj.toString() : "");

                                                                                                Object img1 = document.getData().get("imageUrl1");
                                                                                                Object img2 = document.getData().get("imageUrl2");
                                                                                                Object img3 = document.getData().get("imageUrl3");
                                                                                                Object img4 = document.getData().get("imageUrl4");
                                                                                                Object img5 = document.getData().get("imageUrl5");

                                                                                                dataList.setImageUrl1(img1 != null ? img1.toString() : "");
                                                                                                dataList.setImageUrl2(img2 != null ? img2.toString() : "");
                                                                                                dataList.setImageUrl3(img3 != null ? img3.toString() : "");
                                                                                                dataList.setImageUrl4(img4 != null ? img4.toString() : "");
                                                                                                dataList.setImageUrl5(img5 != null ? img5.toString() : "");

                                                                                                Object nickObj = document.getData().get("nickName");
                                                                                                dataList.setNickName(nickObj != null ? nickObj.toString() : "");

                                                                                                Object categoryObj = document.getData().get("category");
                                                                                                dataList.setCategory(categoryObj != null ? categoryObj.toString() : "");

                                                                                                Object dateObj = document.getData().get("date");
                                                                                                dataList.setDate(dateObj != null ? dateObj.toString() : "");

                                                                                                Object favObj = document.getData().get("favoriteCount");
                                                                                                int favCount = 0;
                                                                                                if (favObj != null) {
                                                                                                    try {
                                                                                                        favCount = Integer.parseInt(favObj.toString());
                                                                                                    } catch (NumberFormatException ne) {
                                                                                                        Log.e("DEBUG_SETINFO", "favoriteCount parse error, value -> " + favObj.toString(), ne);
                                                                                                    }
                                                                                                }
                                                                                                dataList.setFavoriteCount(favCount);

                                                                                                arrayList.add(0, dataList);
                                                                                                Log.e("DEBUG_SETINFO", "post added to arrayList, postId -> " + document.getId());
                                                                                            }
                                                                                        }
                                                                                    }

                                                                                    Log.e("DEBUG_SETINFO", "arrayList size after post load -> " + arrayList.size());
                                                                                    adapter.notifyDataSetChanged();
                                                                                    Log.e("DEBUG_SETINFO", "adapter.notifyDataSetChanged() called (post)");

                                                                                    // Splash Art 없애고 로그인 문구 띄우기
                                                                                    if (nickName != null && !nickName.isEmpty()) {
                                                                                        new Handler().postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Log.e("DEBUG_SETINFO", "Hide splash and show welcome snackbar, nickName -> " + nickName);
                                                                                                getActivity().findViewById(R.id.splish).setVisibility(View.GONE);
                                                                                                Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
                                                                                                Spannable spannable = new SpannableString(String.format("%s 환영합니다.", nickName));
                                                                                                spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                                                                snackbar.setText(spannable);
                                                                                                snackbar.show();
                                                                                                nickName = "";
                                                                                            }
                                                                                        }, 500);
                                                                                    }

                                                                                } else {
                                                                                    Log.e("DEBUG_SETINFO", "post task.getResult() is null");
                                                                                }
                                                                            } else {
                                                                                Log.e("DEBUG_SETINFO", "Error getting post documents", task.getException());
                                                                            }
                                                                        }
                                                                    });

                                                            adapter.notifyDataSetChanged();
                                                            Log.e("DEBUG_SETINFO", "adapter.notifyDataSetChanged() called (like)");
                                                        } else {
                                                            Log.e("DEBUG_SETINFO", "Error getting like documents", task.getException());
                                                        }
                                                    }
                                                });
                                        adapter.notifyDataSetChanged();
                                        Log.e("DEBUG_SETINFO", "adapter.notifyDataSetChanged() called (bookmark)");
                                    } else {
                                        Log.e("DEBUG_SETINFO", "Error getting bookmark documents", task.getException());
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("DEBUG_SETINFO", "onCancelled() called, code = " + error.getCode() + ", message = " + error.getMessage(), error.toException());
                }
            });
        } catch (Exception e) {
            Log.e("DEBUG_SETINFO", "setInfo() error -> " + e.getMessage(), e);
        }
    }


//    public void setInfo() {
//
//        try {
//            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//            final String uid = user.getUid();
//            final FirebaseFirestore db = FirebaseFirestore.getInstance();
//            final ArrayList<String> mainSource = new ArrayList<>();
//
//            arrayList.clear();
//            bookmark.clear();
//            like.clear();
//            mainSource.clear();
//            mainSource.add(uid);
//            Log.e("TAG", "uid ->" + uid);
//
//            mDatabase = FirebaseDatabase.getInstance().getReference("friend/" + uid);
//            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
//                        mainSource.add(postSnapshot.getKey());
//                    }
//                    db.collection("user-checked/" + uid + "/bookmark")
//                            .get()
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.e("TAG", "onFailure ->" + e.getMessage());
//                                }
//                            })
//                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                    if (task.isSuccessful()) {
//                                        Log.e("TAG", "isSuccessful ->" + task.isSuccessful());
//
//                                        for (final QueryDocumentSnapshot document : task.getResult()) {
//                                            bookmark.add(document.getData().get("postID").toString());
//                                        }
//                                        db.collection("user-checked/" + uid + "/like")
//                                                .get()
//                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                        if (task.isSuccessful()) {
//                                                            Log.e("TAG", "isSuccessful ->" + task.isSuccessful());
//                                                            for (final QueryDocumentSnapshot document : task.getResult()) {
//                                                                like.add(document.getData().get("postID").toString());
//                                                            }
//                                                            db.collection("post")
//                                                                    .get()
//                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                                        @Override
//                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                                            if (task.isSuccessful()) {
//                                                                                Log.e("TAG", "isSuccessful ->" + task.isSuccessful());
//                                                                                for (final QueryDocumentSnapshot document : task.getResult()) {
//                                                                                    final Data dataList = new Data();
//                                                                                    for (int j = 0; j < mainSource.size(); j++) {
//                                                                                        if (mainSource.get(j).equals(document.getData().get("uid").toString())) {
//                                                                                            dataList.setBookmark(false);
//                                                                                            for (int i = 0; i < bookmark.size(); i++) {
//                                                                                                if (bookmark.get(i).equals(document.getId())) {
//                                                                                                    dataList.setBookmark(true);
//                                                                                                    break;
//                                                                                                }
//                                                                                            }
//                                                                                            dataList.setLike(false);
//                                                                                            for (int i = 0; i < like.size(); i++) {
//                                                                                                if (like.get(i).equals(document.getId())) {
//                                                                                                    dataList.setLike(true);
//                                                                                                    break;
//                                                                                                }
//                                                                                            }
//
//                                                                                            dataList.setPostID(document.getId());
//                                                                                            dataList.setUid(document.getData().get("uid").toString());
//                                                                                            dataList.setContent(document.getData().get("content").toString());
//                                                                                            dataList.setImageUrl1(document.getData().get("imageUrl1").toString());
//                                                                                            dataList.setImageUrl2(document.getData().get("imageUrl2").toString());
//                                                                                            dataList.setImageUrl3(document.getData().get("imageUrl3").toString());
//                                                                                            dataList.setImageUrl4(document.getData().get("imageUrl4").toString());
//                                                                                            dataList.setImageUrl5(document.getData().get("imageUrl5").toString());
//                                                                                            dataList.setNickName(document.getData().get("nickName").toString());
//                                                                                            dataList.setCategory(document.getData().get("category").toString());
//                                                                                            dataList.setDate(document.getData().get("date").toString());
//                                                                                            dataList.setFavoriteCount(Integer.parseInt(document.getData().get("favoriteCount").toString()));
//                                                                                            arrayList.add(0, dataList);
//
//                                                                                        }
//                                                                                    }
//                                                                                }
//
//                                                                                adapter.notifyDataSetChanged();
//
//                                                                                // Splash Art 없애고 로그인 문구 띄우기
//
//                                                                                if (nickName != null && !nickName.isEmpty()) {
//                                                                                    new Handler().postDelayed(new Runnable() {
//                                                                                        @Override
//                                                                                        public void run() {
//                                                                                            getActivity().findViewById(R.id.splish).setVisibility(View.GONE);
//                                                                                            Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
//                                                                                            Spannable spannable = new SpannableString(String.format("%s 환영합니다.", nickName));
//                                                                                            spannable.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                                                                            snackbar.setText(spannable);
//                                                                                            snackbar.show();
//                                                                                            nickName = "";
//                                                                                        }
//                                                                                    }, 500);
//                                                                                }
//
//
//                                                                            } else {
//                                                                                Log.d("###", "Error getting documents: ", task.getException());
//                                                                            }
//                                                                        }
//                                                                    });
//                                                            adapter.notifyDataSetChanged();
//
//                                                        } else {
//                                                            Log.d("###", "Error getting documents: ", task.getException());
//                                                        }
//                                                    }
//                                                });
//                                        adapter.notifyDataSetChanged();
//                                    } else {
//                                        Log.d("###", "Error getting documents: ", task.getException());
//                                    }
//                                }
//                            });
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        } catch (Exception e) {
//            Log.e("TAG", "error ->" + e.getMessage());
//
//        }
//
//
//    }

    /*
     * 메인에서만 check가 false면 게시글 삭제
     * 북마크 최신업데이트 true면 게시글 수정 업데이트
     * */
    public void refresh(boolean check) {
        if (!check) {
            ((MainActivity) getActivity()).refresh(false);
        }
        contentCheck = check;
    }

    @Override
    public void friendContents(boolean check) {
        setInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSwipeRefreshLayout != null) {
            if (contentCheck) {
                ((MainActivity) getActivity()).refresh(contentCheck);
                contentCheck = false;
            }
        }
    }
}



