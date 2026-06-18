package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;

import java.util.List;

public class ChildModeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 601;
    private static final int REQUEST_READ_IMAGES = 602;
    private Uri pendingImageUri;
    private String pendingTitle = "家人问候";
    private String pendingCategory = "家庭";
    private String pendingMessage = "今天也要开心呀";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(48));
        scroll.addView(root);

        root.addView(titleView("子女模式", 28));
        root.addView(body("安心查看颜爷爷的健康、用药与安全状态"));
        ImageView familyBanner = new ImageView(this);
        familyBanner.setImageResource(R.drawable.family_companion);
        familyBanner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        familyBanner.setContentDescription("家属陪伴插画");
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(-1, dp(150));
        bannerParams.topMargin = dp(16);
        bannerParams.bottomMargin = dp(18);
        root.addView(familyBanner, bannerParams);
        root.addView(spacer(8));
        root.addView(sectionHeader("健康摘要"));
        root.addView(healthSummaryCard());
        root.addView(sectionHeader("今日服药打卡"));
        root.addView(medicineCard());
        root.addView(sectionHeader("紧急提醒记录"));
        root.addView(sosCard());
        root.addView(sectionHeader("家属联系"));
        root.addView(familyCard());
        root.addView(spacer(12));

        Button uploadBtn = btn("为老人上传照片", R.drawable.bg_button_primary, getColor(R.color.surface_white));
        uploadBtn.setOnClickListener(v -> showUploadDialog());
        root.addView(uploadBtn);

        Button backBtn = btn("返回老人端", R.drawable.bg_chip_soft, getColor(R.color.primary_dark));
        backBtn.setOnClickListener(v -> finish());
        root.addView(backBtn);

        setContentView(scroll);
    }

    private void showUploadDialog() {
        pendingImageUri = null;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(32, 8, 32, 0);

        TextView pickBtn = new TextView(this);
        pickBtn.setText("从手机相册选择照片");
        pickBtn.setTextSize(18);
        pickBtn.setTextColor(getColor(R.color.primary));
        pickBtn.setGravity(Gravity.CENTER);
        pickBtn.setBackgroundResource(R.drawable.bg_chip_soft);
        pickBtn.setPadding(dp(14), dp(12), dp(14), dp(12));
        form.addView(pickBtn);

        pickBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_IMAGES);
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_IMAGES);
                    return;
                }
            }
            openGallery();
        });

        new AlertDialog.Builder(this)
            .setTitle("上传照片")
            .setView(form)
            .setPositiveButton("保存", (d, w) -> {
                MockData.addPhoto(pendingTitle, pendingCategory, pendingMessage);
                Toast.makeText(this, "已上传照片到亲情相册，老人端可立即查看", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            pendingImageUri = data.getData();
            Toast.makeText(this, "已选择照片", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "需要相册权限才能选择照片，请在系统设置中授权", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ========== 原有卡片方法 ==========

    private View healthSummaryCard() {
        LinearLayout card = card();
        List<HealthData> data = MockData.getHealthData();
        String[][] keys = {{"heart_rate", "心率"}, {"steps", "步数"}, {"blood_pressure", "血压"}, {"blood_sugar", "血糖"}, {"sleep", "睡眠"}, {"mood", "心情"}};
        int count = 0;
        for (String[] key : keys) {
            for (HealthData h : data) {
                if (h.type.equals(key[0])) {
                    card.addView(metricRow(key[1], h.value + " " + h.getUnit(), h.status));
                    count++;
                    break;
                }
            }
            if (count >= 6) break;
        }
        if (count == 0) card.addView(body("暂无健康数据"));
        return card;
    }

    private View metricRow(String label, String value, String status) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        TextView lv = body(label);
        lv.setTextSize(16);
        row.addView(lv, new LinearLayout.LayoutParams(dp(80), -2));
        TextView vv = new TextView(this);
        vv.setText(value);
        vv.setTextSize(16);
        vv.setTypeface(null, android.graphics.Typeface.BOLD);
        vv.setTextColor(getColor(R.color.text_primary));
        row.addView(vv, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(chipMini(status));
        return row;
    }

    private View medicineCard() {
        LinearLayout card = card();
        List<Medicine> meds = MockData.getMedicines();
        if (meds.isEmpty()) { card.addView(body("暂无药品提醒")); return card; }
        int taken = 0;
        for (Medicine m : meds) if (m.takenToday) taken++;
        TextView s = new TextView(this);
        s.setText("今日完成：" + taken + " / " + meds.size());
        s.setTextSize(18);
        s.setTypeface(null, android.graphics.Typeface.BOLD);
        s.setTextColor(taken == meds.size() ? getColor(R.color.primary) : getColor(R.color.accent_orange));
        card.addView(s);
        for (Medicine m : meds) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));
            TextView nv = body(m.name);
            nv.setTextSize(14);
            row.addView(nv, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(chipMini(m.takenToday ? "已打卡" : "未打卡"));
            card.addView(row);
        }
        return card;
    }

    private View sosCard() {
        LinearLayout card = card();
        List<EmergencyAlert> alerts = MockData.getEmergencyAlerts();
        if (alerts.isEmpty()) { card.addView(body("暂无 SOS 紧急提醒记录")); return card; }
        int show = Math.min(alerts.size(), 3);
        for (int i = 0; i < show; i++) {
            EmergencyAlert a = alerts.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));
            TextView tv = body(a.time);
            tv.setTextSize(14);
            row.addView(tv);
            TextView mv = new TextView(this);
            mv.setText(a.message);
            mv.setTextSize(15);
            mv.setTextColor(getColor(R.color.text_primary));
            row.addView(mv);
            card.addView(row);
        }
        return card;
    }

    private View familyCard() {
        LinearLayout card = card();
        List<FamilyMember> members = MockData.getFamilyMembers();
        if (members.isEmpty()) { card.addView(body("暂无家属信息")); return card; }
        for (FamilyMember m : members) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));
            TextView nv = body(m.name + "（" + m.relationship + "）");
            nv.setTextSize(16);
            row.addView(nv, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(chipMini(m.online ? "在线" : "离线"));
            if (m.phone != null && !m.phone.isEmpty()) {
                TextView pv = body(m.phone);
                pv.setTextSize(14);
                row.addView(pv);
            }
            card.addView(row);
        }
        return card;
    }

    // ========== Utils ==========

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        card.setBackgroundColor(getColor(R.color.surface_white));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(16);
        card.setLayoutParams(p);
        return card;
    }

    private TextView sectionHeader(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(20);
        v.setTypeface(null, android.graphics.Typeface.BOLD);
        v.setTextColor(getColor(R.color.text_primary));
        v.setPadding(0, 0, 0, dp(8));
        return v;
    }

    private TextView titleView(String text, int size) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTypeface(null, android.graphics.Typeface.BOLD);
        v.setTextColor(getColor(R.color.text_primary));
        return v;
    }

    private TextView body(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(15);
        v.setTextColor(getColor(R.color.text_secondary));
        return v;
    }

    private TextView chipMini(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(12);
        v.setTypeface(null, android.graphics.Typeface.BOLD);
        v.setTextColor(getColor(R.color.primary));
        v.setBackgroundResource(R.drawable.bg_chip_soft);
        v.setPadding(dp(10), dp(4), dp(10), dp(4));
        return v;
    }

    private Button btn(String text, int bgRes, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(18);
        b.setAllCaps(false);
        b.setBackgroundResource(bgRes);
        b.setTextColor(textColor);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56));
        p.bottomMargin = dp(10);
        b.setLayoutParams(p);
        return b;
    }

    private View spacer(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, dp(dp)));
        return v;
    }

    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }
}
