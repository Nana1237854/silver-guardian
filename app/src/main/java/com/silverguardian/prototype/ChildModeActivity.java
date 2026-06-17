package com.silverguardian.prototype;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;

public class ChildModeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(getColor(R.color.bg_page));

        root.addView(title("子女模式监控面板"));
        root.addView(card("健康摘要", healthSummary()));
        root.addView(card("今日服药", medicineSummary()));
        root.addView(card("SOS 记录", sosSummary()));

        Button upload = new Button(this);
        upload.setText("为老人上传照片");
        upload.setTextSize(20);
        upload.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("上传照片")
            .setMessage("已模拟上传「家人问候」照片到亲情相册。")
            .setPositiveButton("好的", (d, w) -> MockData.addPhoto("家人问候", "家庭", "今天也要开心呀"))
            .show());
        root.addView(upload, new LinearLayout.LayoutParams(-1, 64));

        Button back = new Button(this);
        back.setText("返回老人端");
        back.setTextSize(20);
        back.setOnClickListener(v -> finish());
        root.addView(back, new LinearLayout.LayoutParams(-1, 64));
        setContentView(root);
    }

    private String healthSummary() {
        StringBuilder builder = new StringBuilder();
        for (HealthData data : MockData.getHealthData()) {
            builder.append(data.getLabel()).append("：").append(data.value).append(data.getUnit()).append("，").append(data.status).append("\n");
            if (builder.length() > 90) break;
        }
        return builder.toString();
    }

    private String medicineSummary() {
        int taken = 0;
        for (Medicine medicine : MockData.getMedicines()) if (medicine.takenToday) taken++;
        return "已打卡 " + taken + " / " + MockData.getMedicines().size() + "\n未打卡药品会显示在老人端用药提醒。";
    }

    private String sosSummary() {
        StringBuilder builder = new StringBuilder();
        for (EmergencyAlert alert : MockData.getEmergencyAlerts()) {
            builder.append(alert.time).append("：").append(alert.message).append("，").append(alert.status).append("\n");
        }
        return builder.length() == 0 ? "暂无 SOS 记录" : builder.toString();
    }

    private TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(26);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getColor(R.color.text_primary));
        return view;
    }

    private TextView card(String heading, String body) {
        TextView view = new TextView(this);
        view.setText(heading + "\n" + body);
        view.setTextSize(20);
        view.setTextColor(getColor(R.color.text_primary));
        view.setPadding(24, 22, 24, 22);
        view.setBackgroundColor(getColor(R.color.surface_white));
        return view;
    }
}
