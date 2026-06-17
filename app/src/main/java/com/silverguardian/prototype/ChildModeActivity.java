package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;

import java.util.List;

/**
 * 子女监控面板 — 家属查看老人的健康、用药、SOS 记录。
 */
public class ChildModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(48));
        scroll.addView(root);

        // 标题
        root.addView(titleView("子女监控面板", 26));
        root.addView(body("实时查看老人的健康数据与用药情况"));
        root.addView(spacer(8));

        // 健康摘要卡片
        root.addView(sectionHeader("❤️ 健康摘要"));
        root.addView(healthSummaryCard());

        // 用药打卡
        root.addView(sectionHeader("💊 今日服药打卡"));
        root.addView(medicineCard());

        // SOS 记录
        root.addView(sectionHeader("🚨 紧急提醒记录"));
        root.addView(sosCard());

        // 家属列表
        root.addView(sectionHeader("👨‍👩‍👧‍👦 家属"));
        root.addView(familyCard());

        // 操作按钮
        root.addView(spacer(12));
        Button uploadBtn = btn("📷 为老人上传照片", R.drawable.bg_button_primary, getColor(R.color.surface_white));
        uploadBtn.setOnClickListener(v -> {
            MockData.addPhoto("家人问候", "家庭", "今天也要开心呀");
            new AlertDialog.Builder(this)
                .setTitle("上传成功")
                .setMessage("已上传「家人问候」照片到亲情相册，老人端可立即查看。")
                .setPositiveButton("好的", null)
                .show();
        });
        root.addView(uploadBtn);

        Button backBtn = btn("↩ 返回老人端", R.drawable.bg_chip_soft, getColor(R.color.primary));
        backBtn.setOnClickListener(v -> finish());
        root.addView(backBtn);

        setContentView(scroll);
    }

    // ========== Health Summary ==========

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
        if (count == 0) {
            TextView empty = body("暂无健康数据");
            card.addView(empty);
        }
        return card;
    }

    private View metricRow(String label, String value, String status) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView labelView = body(label);
        labelView.setTextSize(16);
        row.addView(labelView, new LinearLayout.LayoutParams(dp(80), -2));

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(16);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setTextColor(getColor(R.color.text_primary));
        row.addView(valueView, new LinearLayout.LayoutParams(0, -2, 1));

        TextView statusView = chipMini(status);
        row.addView(statusView);
        return row;
    }

    // ========== Medicine Check-in ==========

    private View medicineCard() {
        LinearLayout card = card();

        List<Medicine> meds = MockData.getMedicines();
        if (meds.isEmpty()) {
            card.addView(body("暂无药品提醒"));
            return card;
        }
        int taken = 0;
        for (Medicine m : meds) if (m.takenToday) taken++;

        TextView summary = new TextView(this);
        summary.setText("今日完成：" + taken + " / " + meds.size());
        summary.setTextSize(18);
        summary.setTypeface(null, android.graphics.Typeface.BOLD);
        summary.setTextColor(taken == meds.size() ? getColor(R.color.primary) : getColor(R.color.accent_orange));
        card.addView(summary);

        for (Medicine m : meds) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView nameView = body(m.name);
            nameView.setTextSize(15);
            row.addView(nameView, new LinearLayout.LayoutParams(0, -2, 1));

            TextView statusView = chipMini(m.takenToday ? "✅ 已打卡" : "⏳ 未打卡");
            row.addView(statusView);
            card.addView(row);
        }
        return card;
    }

    // ========== SOS Records ==========

    private View sosCard() {
        LinearLayout card = card();

        List<EmergencyAlert> alerts = MockData.getEmergencyAlerts();
        if (alerts.isEmpty()) {
            card.addView(body("暂无 SOS 紧急提醒记录"));
            return card;
        }
        int show = Math.min(alerts.size(), 3);
        for (int i = 0; i < show; i++) {
            EmergencyAlert a = alerts.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView timeView = body(a.time);
            timeView.setTextSize(13);
            row.addView(timeView);

            TextView msgView = new TextView(this);
            msgView.setText(a.message);
            msgView.setTextSize(15);
            msgView.setTextColor(getColor(R.color.text_primary));
            row.addView(msgView);

            card.addView(row);
        }
        return card;
    }

    // ========== Family Members ==========

    private View familyCard() {
        LinearLayout card = card();

        List<FamilyMember> members = MockData.getFamilyMembers();
        if (members.isEmpty()) {
            card.addView(body("暂无家属信息"));
            return card;
        }
        for (FamilyMember m : members) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView nameView = body(m.name + "（" + m.relationship + "）");
            nameView.setTextSize(16);
            row.addView(nameView, new LinearLayout.LayoutParams(0, -2, 1));

            TextView onlineView = chipMini(m.online ? "在线" : "离线");
            row.addView(onlineView);

            if (m.phone != null && !m.phone.isEmpty()) {
                TextView phoneView = body(" 📞 " + m.phone);
                phoneView.setTextSize(14);
                row.addView(phoneView);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(16);
        card.setLayoutParams(params);
        return card;
    }

    private TextView sectionHeader(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(20);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(0, 0, 0, dp(8));
        return view;
    }

    private TextView titleView(String text, int size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(getColor(R.color.text_secondary));
        return view;
    }

    private TextView chipMini(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(12);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(10), dp(4), dp(10), dp(4));
        return view;
    }

    private Button btn(String text, int bgRes, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setAllCaps(false);
        button.setBackgroundResource(bgRes);
        button.setTextColor(textColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.bottomMargin = dp(10);
        button.setLayoutParams(params);
        return button;
    }

    private View spacer(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, dp(dp)));
        return v;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
