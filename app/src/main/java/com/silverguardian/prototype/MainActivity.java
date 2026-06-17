package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.fragments.AlbumFragment;
import com.silverguardian.prototype.fragments.HealthFragment;
import com.silverguardian.prototype.reminder.TtsHelper;
import com.silverguardian.prototype.fragments.HomeFragment;
import com.silverguardian.prototype.fragments.MedicineFragment;
import com.silverguardian.prototype.fragments.SettingsFragment;
import com.silverguardian.prototype.models.FamilyMember;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PHONE = 101;
    private HealthFragment healthFragment;
    private MedicineFragment medicineFragment;
    private HomeFragment homeFragment;
    private AlbumFragment albumFragment;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        TtsHelper.init(this);  // 预初始化TTS，确保BroadcastReceiver触发时已就绪
        int userId = getIntent().getIntExtra("user_id", 1);
        MockData.setActiveUser(userId);
        setContentView(R.layout.activity_main);

        healthFragment = new HealthFragment();
        medicineFragment = new MedicineFragment();
        homeFragment = new HomeFragment();
        albumFragment = new AlbumFragment();
        settingsFragment = new SettingsFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_health) { switchFragment(healthFragment); return true; }
            if (id == R.id.bottom_medicine) { switchFragment(medicineFragment); return true; }
            if (id == R.id.bottom_home) { switchFragment(homeFragment); return true; }
            if (id == R.id.bottom_album) { switchFragment(albumFragment); return true; }
            if (id == R.id.bottom_settings) { switchFragment(settingsFragment); return true; }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.bottom_home);
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TtsHelper.release();
    }

    public void openHealth() {
        ((BottomNavigationView) findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.bottom_health);
    }

    public void switchToFraud() {
        switchFragment(new com.silverguardian.prototype.fragments.FraudFragment());
    }

    public void switchToMemory() {
        switchFragment(new com.silverguardian.prototype.fragments.MemoryFragment());
    }

    // === 一键呼叫 ===
    public void oneTapCall() {
        List<FamilyMember> members = MockData.getFamilyMembers();
        String phone = null;
        for (FamilyMember m : members) {
            if (m.online && m.phone != null && !m.phone.isEmpty()) {
                phone = m.phone;
                break;
            }
        }
        if (phone == null) {
            Toast.makeText(this, "没有可用的家属号码", Toast.LENGTH_SHORT).show();
            return;
        }
        makePhoneCall(phone);
    }

    public void oneTapCallLongPress() {
        List<FamilyMember> members = MockData.getFamilyMembers();
        String[] names = new String[members.size() + 1];
        String[] phones = new String[members.size() + 1];
        for (int i = 0; i < members.size(); i++) {
            names[i] = members.get(i).name + "（" + members.get(i).relationship + "）";
            phones[i] = members.get(i).phone;
        }
        names[members.size()] = "120 急救中心";
        phones[members.size()] = "120";

        new AlertDialog.Builder(this)
            .setTitle("选择呼叫对象")
            .setItems(names, (d, which) -> makePhoneCall(phones[which]))
            .setNegativeButton("取消", null)
            .show();
    }

    private void makePhoneCall(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "该联系人无电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            pendingCallPhone = phone;
            return;
        }
        doCall(phone);
    }

    private String pendingCallPhone = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCallPhone != null) doCall(pendingCallPhone);
            } else {
                Toast.makeText(this, "需要电话权限才能一键呼叫，请在系统设置中授予", Toast.LENGTH_LONG).show();
            }
            pendingCallPhone = null;
        }
    }

    private void doCall(String phone) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }
}
