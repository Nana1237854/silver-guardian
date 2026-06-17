package com.silverguardian.prototype;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.silverguardian.prototype.fragments.CommunityFragment;

public class CommunityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new CommunityFragment())
            .commit();
    }
}
