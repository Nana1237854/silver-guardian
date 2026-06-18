package com.silverguardian.prototype.health;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.util.List;

/**
 * 健康异常自动告警。当健康指标超出安全范围时，发出通知并记录。
 */
public class HealthAlertService {
    private static final String CHANNEL_ID = "health_alert";
    private static final String CHANNEL_NAME = "健康异常告警";
    private static int alertIdCounter = 200;

    public static void initChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("心率、血压等健康指标异常时发出告警");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /** 检查健康数据是否异常，异常则告警 */
    public static void checkAndAlert(Context context, HealthData data) {
        AlertLevel level = evaluate(data);
        if (level == AlertLevel.NORMAL) return;

        String message = buildAlertMessage(data, level);
        Repository repo = Repository.getInstance();
        repo.addEmergencyAlert(message);
        showNotification(context, data, message);

        // 语音播报告警
        TtsHelper.speak("健康提醒：" + message);

        // 家属通知（本地模拟：记录到家属可见的告警列表）
        List<FamilyMember> members = repo.getFamilyMembers();
        if (!members.isEmpty()) {
            String familyMsg = "已通知家属：" + members.get(0).name;
            if (members.size() > 1) familyMsg += "、" + members.get(1).name;
            repo.addEmergencyAlert(familyMsg);
        }
    }

    private static AlertLevel evaluate(HealthData data) {
        String type = data.type;
        String value = data.value;

        try {
            switch (type) {
                case "heart_rate": {
                    int hr = Integer.parseInt(value.replaceAll("[^0-9]", ""));
                    if (hr < 45) return AlertLevel.CRITICAL;
                    if (hr < 55) return AlertLevel.WARNING;
                    if (hr > 130) return AlertLevel.CRITICAL;
                    if (hr > 110) return AlertLevel.WARNING;
                    return AlertLevel.NORMAL;
                }
                case "blood_pressure": {
                    // 格式: "128/82"
                    String[] parts = value.split("/");
                    if (parts.length >= 2) {
                        int systolic = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                        int diastolic = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                        if (systolic > 180 || diastolic > 120) return AlertLevel.CRITICAL;
                        if (systolic > 160 || diastolic > 100) return AlertLevel.WARNING;
                        if (systolic < 80) return AlertLevel.WARNING;
                    }
                    return AlertLevel.NORMAL;
                }
                case "blood_oxygen": {
                    int spo2 = Integer.parseInt(value.replaceAll("[^0-9]", ""));
                    if (spo2 < 90) return AlertLevel.CRITICAL;
                    if (spo2 < 94) return AlertLevel.WARNING;
                    return AlertLevel.NORMAL;
                }
                case "temperature": {
                    double temp = Double.parseDouble(value.replaceAll("[^0-9.]", ""));
                    if (temp > 39.0) return AlertLevel.CRITICAL;
                    if (temp > 38.0) return AlertLevel.WARNING;
                    return AlertLevel.NORMAL;
                }
                case "blood_sugar": {
                    double sugar = Double.parseDouble(value.replaceAll("[^0-9.]", ""));
                    if (sugar > 16.0 || sugar < 3.0) return AlertLevel.CRITICAL;
                    if (sugar > 11.0 || sugar < 4.0) return AlertLevel.WARNING;
                    return AlertLevel.NORMAL;
                }
                case "respiratory_rate": {
                    int rr = Integer.parseInt(value.replaceAll("[^0-9]", ""));
                    if (rr > 30 || rr < 8) return AlertLevel.CRITICAL;
                    if (rr > 24 || rr < 10) return AlertLevel.WARNING;
                    return AlertLevel.NORMAL;
                }
                default:
                    return AlertLevel.NORMAL;
            }
        } catch (NumberFormatException e) {
            return AlertLevel.NORMAL; // 无法解析则不告警
        }
    }

    private static String buildAlertMessage(HealthData data, AlertLevel level) {
        String label = data.getLabel();
        String value = data.value;
        String unit = data.getUnit();
        String prefix = level == AlertLevel.CRITICAL ? "⚠️ 紧急" : "注意";
        return prefix + "：" + label + " " + value + " " + unit + "，建议联系医生或家属。";
    }

    private static void showNotification(Context context, HealthData data, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("initial_tab", "health");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, alertIdCounter,
            intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_health)
            .setContentTitle("健康异常告警")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(alertIdCounter++, builder.build());
        } catch (SecurityException e) {
            // 通知权限未授予，静默跳过
        }
    }

    enum AlertLevel { NORMAL, WARNING, CRITICAL }
}
