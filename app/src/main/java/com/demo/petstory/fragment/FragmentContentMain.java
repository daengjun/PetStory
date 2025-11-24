package com.demo.petstory.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.demo.petstory.R;
import com.demo.petstory.activity.MainActivity;
import com.demo.petstory.activity.SettingBlockFriendsActivity;
import com.demo.petstory.activity.SettingBookMarkActivity;
import com.demo.petstory.util.calbacklistener;


/**
 * Container Fragment
 * 친구 목록 , 채팅방 목록
 **/
public class FragmentContentMain extends Fragment implements calbacklistener {

    ViewGroup viewGroup;
    boolean contentCheck;
    boolean onCreate = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_content_main, container, false);
        onCreate = true;
        SettingBookMarkActivity.setlistener(this);
        SettingBlockFriendsActivity.setlistener(this);

        final Button cb = viewGroup.findViewById(R.id.CBs);
        final Button fb = viewGroup.findViewById(R.id.FBs);

        NavController navController = NavHostFragment.findNavController(
                (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_host_fragment)
        );

        Log.e("TAG", "cb object -> " + cb);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.setTextColor(Color.parseColor("#000000"));
                fb.setTextColor(Color.parseColor("#2A000000"));
                cb.setBackgroundResource(R.drawable.button_on);
                fb.setBackgroundResource(R.drawable.button_off);
                navController.navigate(R.id.nav_chat);

            }
        });

        Log.e("TAG", "fb object: -> " + cb);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fb.setBackgroundResource(R.drawable.button_on);
                cb.setBackgroundResource(R.drawable.button_off);
                fb.setTextColor(Color.parseColor("#000000"));
                cb.setTextColor(Color.parseColor("#2A000000"));
                navController.navigate(R.id.nav_friends);
            }
        });

        return viewGroup;
    }

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
    public void friendContents(boolean check) {}

    @Override
    public void onResume() {
        super.onResume();
        if (onCreate) {
            if (contentCheck) {
                ((MainActivity) getActivity()).refresh(contentCheck);
                contentCheck = false;
            }
        }
    }
}
