package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.fragments.AlbumFragment;
import com.silverguardian.prototype.fragments.FraudFragment;
import com.silverguardian.prototype.fragments.HealthFragment;
import com.silverguardian.prototype.fragments.HomeFragment;
import com.silverguardian.prototype.fragments.MedicineFragment;
import com.silverguardian.prototype.fragments.MemoryFragment;
import com.silverguardian.prototype.fragments.SettingsFragment;
import com.silverguardian.prototype.health.HealthAlertService;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.util.List;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_CALL_PHONE = 101;

    private static final String TAB_HEALTH = "health";
    private static final String TAB_MEDICINE = "medicine";
    private static final String TAB_ALBUM = "album";
    private static final String TAB_SETTINGS = "settings";
    private static final String TAB_FRAUD = "fraud";
    private static final String TAB_MEMORY = "memory";

    private HealthFragment healthFragment;
    private MedicineFragment medicineFragment;
    private HomeFragment homeFragment;
    private AlbumFragment albumFragment;
    private SettingsFragment settingsFragment;
    private BottomNavigationView bottomNavigation;
    private String pendingCallPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        TtsHelper.init(this);
        HealthAlertService.initChannel(this);

        int userId = getIntent().getIntExtra("user_id", 1);
        MockData.setActiveUser(userId);
        setContentView(R.layout.activity_main);

        initFragments();
        bindBottomNavigation();
        applyInitialDestination(getIntent().getStringExtra("initial_tab"));
    }

    private void initFragments() {
        healthFragment = new HealthFragment();
        medicineFragment = new MedicineFragment();
        homeFragment = new HomeFragment();
        albumFragment = new AlbumFragment();
        settingsFragment = new SettingsFragment();

        String initialAlbum = getIntent().getStringExtra("album_name");
        if (initialAlbum != null && !initialAlbum.trim().isEmpty()) {
            Bundle albumArgs = new Bundle();
            albumArgs.putString("album_name", initialAlbum);
            albumFragment.setArguments(albumArgs);
        }
    }

    private void bindBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            showPrimaryDestination(item.getItemId());
            return true;
        });
    }

    private void applyInitialDestination(String initialTab) {
        if (TAB_FRAUD.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_settings);
            showContextFragment(new FraudFragment());
            return;
        }
        if (TAB_MEMORY.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_settings);
            showContextFragment(new MemoryFragment());
            return;
        }
        if (TAB_HEALTH.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_health);
            return;
        }
        if (TAB_MEDICINE.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_medicine);
            return;
        }
        if (TAB_ALBUM.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_album);
            return;
        }
        if (TAB_SETTINGS.equals(initialTab)) {
            bottomNavigation.setSelectedItemId(R.id.bottom_settings);
            return;
        }
        bottomNavigation.setSelectedItemId(R.id.bottom_home);
    }

    private void showPrimaryDestination(int itemId) {
        if (itemId == R.id.bottom_health) {
            switchFragment(healthFragment);
            return;
        }
        if (itemId == R.id.bottom_medicine) {
            switchFragment(medicineFragment);
            return;
        }
        if (itemId == R.id.bottom_album) {
            switchFragment(albumFragment);
            return;
        }
        if (itemId == R.id.bottom_settings) {
            switchFragment(settingsFragment);
            return;
        }
        switchFragment(homeFragment);
    }

    private void showContextFragment(Fragment fragment) {
        switchFragment(fragment);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TtsHelper.release();
    }

    public void openHealth() {
        bottomNavigation.setSelectedItemId(R.id.bottom_health);
    }

    public void openMedicine() {
        bottomNavigation.setSelectedItemId(R.id.bottom_medicine);
    }

    public void openAlbum() {
        bottomNavigation.setSelectedItemId(R.id.bottom_album);
    }

    public void openSettings() {
        bottomNavigation.setSelectedItemId(R.id.bottom_settings);
    }

    public void openHome() {
        bottomNavigation.setSelectedItemId(R.id.bottom_home);
    }

    public void switchToFraud() {
        bottomNavigation.setSelectedItemId(R.id.bottom_settings);
        showContextFragment(new FraudFragment());
    }

    public void switchToMemory() {
        bottomNavigation.setSelectedItemId(R.id.bottom_settings);
        showContextFragment(new MemoryFragment());
    }

    public void oneTapCall() {
        List<FamilyMember> members = MockData.getFamilyMembers();
        String phone = null;
        for (FamilyMember member : members) {
            if (member.online && member.phone != null && !member.phone.isEmpty()) {
                phone = member.phone;
                break;
            }
        }
        if (phone == null) {
            Toast.makeText(this, "\u6ca1\u6709\u53ef\u7528\u7684\u5bb6\u5c5e\u53f7\u7801", Toast.LENGTH_SHORT).show();
            return;
        }
        makePhoneCall(phone);
    }

    public void oneTapCallLongPress() {
        List<FamilyMember> members = MockData.getFamilyMembers();
        String[] names = new String[members.size() + 1];
        String[] phones = new String[members.size() + 1];
        for (int i = 0; i < members.size(); i++) {
            names[i] = members.get(i).name + "\uff08" + members.get(i).relationship + "\uff09";
            phones[i] = members.get(i).phone;
        }
        names[members.size()] = "120 \u6025\u6551\u4e2d\u5fc3";
        phones[members.size()] = "120";

        new AlertDialog.Builder(this)
            .setTitle("\u9009\u62e9\u547c\u53eb\u5bf9\u8c61")
            .setItems(names, (dialog, which) -> makePhoneCall(phones[which]))
            .setNegativeButton("\u53d6\u6d88", null)
            .show();
    }

    private void makePhoneCall(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "\u8be5\u8054\u7cfb\u4eba\u65e0\u7535\u8bdd\u53f7\u7801", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            pendingCallPhone = phone;
            return;
        }
        doCall(phone);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCallPhone != null) {
                    doCall(pendingCallPhone);
                }
            } else {
                Toast.makeText(this, "\u9700\u8981\u7535\u8bdd\u6743\u9650\u624d\u80fd\u4e00\u952e\u547c\u53eb\uff0c\u8bf7\u5728\u7cfb\u7edf\u8bbe\u7f6e\u4e2d\u6388\u4e88", Toast.LENGTH_LONG).show();
            }
            pendingCallPhone = null;
        }
    }

    private void doCall(String phone) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }
}
