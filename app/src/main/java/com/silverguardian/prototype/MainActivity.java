package com.silverguardian.prototype;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.fragments.AlbumFragment;
import com.silverguardian.prototype.fragments.HealthFragment;
import com.silverguardian.prototype.fragments.MedicineFragment;
import com.silverguardian.prototype.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private HealthFragment healthFragment;
    private MedicineFragment medicineFragment;
    private AlbumFragment albumFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        int userId = getIntent().getIntExtra("user_id", 1);
        MockData.setActiveUser(userId);
        setContentView(R.layout.activity_main);

        healthFragment = new HealthFragment();
        medicineFragment = new MedicineFragment();
        albumFragment = new AlbumFragment();
        settingsFragment = new SettingsFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_health) {
                switchFragment(healthFragment);
                return true;
            }
            if (id == R.id.bottom_medicine) {
                switchFragment(medicineFragment);
                return true;
            }
            if (id == R.id.bottom_home) {
                startActivity(new Intent(this, ChatDetailActivity.class).putExtra("chat_title", "银发守护助手"));
                return false;
            }
            if (id == R.id.bottom_album) {
                switchFragment(albumFragment);
                return true;
            }
            if (id == R.id.bottom_settings) {
                switchFragment(settingsFragment);
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.bottom_health);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    public void openHealth() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_health);
    }
}
