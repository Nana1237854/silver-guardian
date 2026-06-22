package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.silverguardian.prototype.fragments.AlbumFragment;
import com.silverguardian.prototype.fragments.FraudFragment;
import com.silverguardian.prototype.fragments.HealthFragment;
import com.silverguardian.prototype.fragments.HomeFragment;
import com.silverguardian.prototype.fragments.MedicineFragment;
import com.silverguardian.prototype.fragments.MemoryFragment;
import com.silverguardian.prototype.fragments.SettingsFragment;
import com.silverguardian.prototype.health.HealthAlertService;
import com.silverguardian.prototype.models.FamilyMember;

import java.util.List;

// 主页面容器：底部导航栏、Fragment切换管理
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

    // 主页面初始化：设置当前用户、初始化Fragment、绑定底部导航
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HealthAlertService.initChannel(this);
        int userId = getIntent().getIntExtra("user_id", 1);
        userSession().setActiveUser(userId);
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

    // 设置底部导航栏选中切换监听
    private void bindBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            showPrimaryDestination(item.getItemId());
            return true;
        });
    }

    private void applyInitialDestination(String initialTab) {
        if (TAB_FRAUD.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_settings); showContextFragment(new FraudFragment()); return; }
        if (TAB_MEMORY.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_settings); showContextFragment(new MemoryFragment()); return; }
        if (TAB_HEALTH.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_health); return; }
        if (TAB_MEDICINE.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_medicine); return; }
        if (TAB_ALBUM.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_album); return; }
        if (TAB_SETTINGS.equals(initialTab)) { bottomNavigation.setSelectedItemId(R.id.bottom_settings); return; }
        bottomNavigation.setSelectedItemId(R.id.bottom_home);
    }

    private void showPrimaryDestination(int itemId) {
        if (itemId == R.id.bottom_health) { switchFragment(healthFragment); return; }
        if (itemId == R.id.bottom_medicine) { switchFragment(medicineFragment); return; }
        if (itemId == R.id.bottom_album) { switchFragment(albumFragment); return; }
        if (itemId == R.id.bottom_settings) { switchFragment(settingsFragment); return; }
        switchFragment(homeFragment);
    }

    private void showContextFragment(Fragment fragment) { switchFragment(fragment); }
    private void switchFragment(Fragment fragment) { getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit(); }
    public void openHealth() { bottomNavigation.setSelectedItemId(R.id.bottom_health); }
    public void openMedicine() { bottomNavigation.setSelectedItemId(R.id.bottom_medicine); }
    public void openAlbum() { bottomNavigation.setSelectedItemId(R.id.bottom_album); }
    public void openSettings() { bottomNavigation.setSelectedItemId(R.id.bottom_settings); }
    public void openHome() { bottomNavigation.setSelectedItemId(R.id.bottom_home); }
    public void switchToFraud() { bottomNavigation.setSelectedItemId(R.id.bottom_settings); showContextFragment(new FraudFragment()); }
    public void switchToMemory() { bottomNavigation.setSelectedItemId(R.id.bottom_settings); showContextFragment(new MemoryFragment()); }

    public void oneTapCall() {
        String phone = userSession().findPrimaryFamilyPhone();
        if (phone == null) {
            Toast.makeText(this, "No family number available", Toast.LENGTH_SHORT).show();
            return;
        }
        makePhoneCall(phone);
    }

    public void oneTapCallLongPress() {
        List<FamilyMember> members = userSession().getFamilyMembers();
        String[] names = new String[members.size() + 1];
        String[] phones = new String[members.size() + 1];
        for (int i = 0; i < members.size(); i++) {
            names[i] = members.get(i).name + " (" + members.get(i).relationship + ")";
            phones[i] = members.get(i).phone;
        }
        names[members.size()] = "120 Emergency";
        phones[members.size()] = "120";
        new AlertDialog.Builder(this)
            .setTitle("Choose contact")
            .setItems(names, (dialog, which) -> makePhoneCall(phones[which]))
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void showOneTapChooser() {
        List<FamilyMember> members = userSession().getFamilyMembers();
        if (members.isEmpty()) { Toast.makeText(this, "还没有家属联系方式", Toast.LENGTH_SHORT).show(); return; }
        LinearLayout buttons = new LinearLayout(this); buttons.setOrientation(LinearLayout.VERTICAL); buttons.setPadding(24, 8, 24, 8);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选择家属").setView(buttons).setNegativeButton(R.string.common_cancel, null).create();
        for (FamilyMember member : members) {
            Button button = new Button(this); button.setText(member.name + "（" + member.relationship + "）");
            button.setMinHeight((int) (60 * getResources().getDisplayMetrics().density));
            button.setOnClickListener(v -> { dialog.dismiss(); makePhoneCall(member.phone); }); buttons.addView(button);
        }
        dialog.show();
    }

    public void showSosDialog() {
        TextView countdown = new TextView(this); countdown.setTextSize(22); countdown.setPadding(32, 24, 32, 24);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("是否紧急呼叫？").setView(countdown)
            .setPositiveButton("确认呼叫", null).setNegativeButton("取消", null).create();
        android.os.CountDownTimer timer = new android.os.CountDownTimer(10000, 1000) {
            public void onTick(long ms) { countdown.setText((ms / 1000 + 1) + " 秒后自动拨打课程测试号码 10086"); }
            public void onFinish() { if (dialog.isShowing()) { dialog.dismiss(); makePhoneCall("10086"); } }
        };
        dialog.setOnShowListener(x -> { timer.start(); dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> { timer.cancel(); dialog.dismiss(); showSosContacts(); }); });
        dialog.setOnDismissListener(x -> timer.cancel()); dialog.show();
    }

    private void showSosContacts() {
        List<FamilyMember> members = userSession().getFamilyMembers(); String[] names = new String[members.size() + 1];
        names[0] = "课程测试号码 10086"; for (int i = 0; i < members.size(); i++) names[i + 1] = members.get(i).name;
        new AlertDialog.Builder(this).setTitle("选择首位联系人").setItems(names, (d, which) -> makePhoneCall(which == 0 ? "10086" : members.get(which - 1).phone)).setNegativeButton(R.string.common_cancel, null).show();
    }
    private void makePhoneCall(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "This contact has no phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            pendingCallPhone = phone;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingCallPhone != null) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + pendingCallPhone)));
            } else {
                Toast.makeText(this, "Phone permission is required for one-tap call", Toast.LENGTH_LONG).show();
            }
            pendingCallPhone = null;
        }
    }
}