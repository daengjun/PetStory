package com.demo.petstory.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demo.petstory.model.BlockFriendInfo;
import com.demo.petstory.util.ItemTouchHelperListener;
import com.demo.petstory.R;
import com.demo.petstory.util.util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BlockFriendsAdapter extends RecyclerView.Adapter<BlockFriendsAdapter.ViewHolder> implements ItemTouchHelperListener , com.demo.petstory.util.calbacklistener {

    ArrayList<BlockFriendInfo> items = new ArrayList<BlockFriendInfo>();
    private OnItemClickListener mListener = null;
    private Context mContext;
    com.demo.petstory.util.calbacklistener calbacklistener;

    public BlockFriendsAdapter(Context mContext, com.demo.petstory.util.calbacklistener calbacklistener){
        this.mContext = mContext;
        this.calbacklistener = calbacklistener;
    }

    @Override
    public void refresh(boolean check) {}

    @Override
    public void friendContents(boolean check) {}

    public interface OnItemClickListener{
        void onItemClick(View v, int position);

    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView nick;
        ImageView profile;

        public ViewHolder(final View itemView){
            super(itemView);

            nick = itemView.findViewById(R.id.textView);
            textView = itemView.findViewById(R.id.tvChat);
            profile = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        BlockFriendInfo item = items.get(pos);
                        if(mListener != null){
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
        }
        public void setItem(String nickName){
            nick.setText(nickName);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.block_person_item, viewGroup, false);

        return new ViewHolder(itemView);

    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder , @SuppressLint("RecyclerView") final int position){

        final BlockFriendInfo item = items.get(position);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getId().equals(item.getFriendUid())){
                                    viewHolder.setItem(document.getData().get("nickName").toString());
                                    if(document.getData().get("profileImg").toString() != null && document.getData().get("profileImg").toString().length() > 0){
                                        ImageView profileView = viewHolder.itemView.findViewById(R.id.imageView);
                                        Glide.with(mContext).load(document.getData().get("profileImg").toString()).centerCrop().override(500).into(profileView);
                                    }
                                    break;
                                }
                            }
                        } else {
                            Log.d("###", "Error getting documents: ", task.getException());
                        }
                    }
                });

        Button unblock;
        unblock = viewHolder.itemView.findViewById(R.id.btn_unblock);

        unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("blockFriends/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/friends").document(item.getFriendUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                items.remove(position);
                                notifyItemRemoved(position);
                                //this line below gives you the animation and also updates the
                                //list items after the deleted item
                                notifyItemRangeChanged(position, getItemCount());
                                util.snackbar(mContext,"차단해제 성공", Snackbar.LENGTH_SHORT);
//                                Toast.makeText(mContext, "차단해제 성공", Toast.LENGTH_SHORT).show();
                                calbacklistener.refresh(true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                util.snackbar(mContext,"차단해제 실패", Snackbar.LENGTH_SHORT);
//                                Toast.makeText(mContext, "차단해제 실패", Toast.LENGTH_SHORT).show();
                                calbacklistener.refresh(true);
                            }
                        });
            }
        });
    }
    @Override
    public int getItemCount(){
        return items.size();
    }
    public void addItem(BlockFriendInfo item){
        items.add(item);
    }
    public void setItems(ArrayList<BlockFriendInfo> items){
        this.items  = items;
    }
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }

    public boolean onItemMove(int from_position, int to_position) {
        BlockFriendInfo person = items.get(from_position);
        items.remove(from_position);
        items.add(to_position,person);
        notifyItemMoved(from_position,to_position);
        return true;
    }

    public void onItemSwipe(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onRightClick(int position, RecyclerView.ViewHolder viewHolder) {
        items.remove(position);
        notifyItemRemoved(position);
    }
}
