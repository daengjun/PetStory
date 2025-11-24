package com.demo.petstory.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.petstory.util.ItemTouchHelperCallback;
import com.demo.petstory.model.Person;
import com.demo.petstory.adapter.PersonAdapter;
import com.demo.petstory.R;
import com.demo.petstory.activity.MainActivity;
import com.demo.petstory.util.calbacklistener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;

/**
 * 친구 목록
 **/
public class FriendsListFragment extends Fragment implements calbacklistener {

    private FirebaseDatabase database;
    private static final String TAG = "FriendsListFragment";
    ArrayList<Person> personArrayList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.fri_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        final PersonAdapter adapter = new PersonAdapter(getContext(), this);

        database = FirebaseDatabase.getInstance();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        personArrayList = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        if (document.getId().equals(dataSnapshot.getKey())) {

                                            Person person = dataSnapshot.getValue(Person.class);
                                            personArrayList.add(person);
                                            adapter.addItem(new Person(document.get("nickName").toString(), document.getId()));
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                }
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

        DatabaseReference ref = database.getReference("friend").child(user.getUid());
        ref.addChildEventListener(childEventListener);

        recyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(recyclerView);

        return root;
    }

    /*
     * 친구 목록 삭제하면 전체 실시간 업뎃 진행
     * */
    public void refresh(boolean check) {
        Log.d(TAG, "dangjun friend Update");
        ((MainActivity) getActivity()).refresh(check);

    }

    @Override
    public void friendContents(boolean check) {}

}