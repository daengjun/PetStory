package com.demo.petstory.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.BuildConfig;
import com.demo.petstory.R;

/**
 * 앱 정보
 **/
public class SettingAppInfoActivity extends AppCompatActivity {

    private TextView app_version, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app_version = findViewById(R.id.app_version);
        description = findViewById(R.id.description);

//        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        String appVersionName = BuildConfig.VERSION_NAME;
        app_version.setText("Version : " + appVersionName);

        description.setText("PetStory is a simple and engaging social app for pet lovers." +
                " Users can upload photos of their pets and share updates" +
                "about their pet's daily life. \n\nConnect with fellow" +
                "pet enthusiasts and enjoy a vibrant community dedicated to celebrating pets.");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
