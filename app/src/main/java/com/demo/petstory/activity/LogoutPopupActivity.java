package com.demo.petstory.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.demo.petstory.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_logout_popup);

    }

    public void okLogout(View v){
        FirebaseAuth.getInstance().signOut();
        myStartActivity(LoginActivity.class);
        finish();
    }

    public void cancelLogout(View v){
        finish();
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}

