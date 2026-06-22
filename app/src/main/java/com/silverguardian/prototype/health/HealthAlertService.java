package com.silverguardian.prototype.health;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.notification.AndroidNotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationRequest;
import com.silverguardian.prototype.tts.AndroidTtsAdapter;
import com.silverguardian.prototype.tts.TtsAdapter;

import java.util.List;

// 健康异常告警服务：通知推送、TTS播报、家属端异常记录
public class HealthAlertService {
    private static final String CHANNEL_ID = "health_alert";
    private static final String CHANNEL_NAME = "健康异常告警";
    private static final NotificationDispatcher notificationDispatcher =
        new AndroidNotificationDispatcher();
    private static final TtsAdapter ttsAdapter = new AndroidTtsAdapter();
    private static int alertIdCounter = 200;

    // 检查健康数据是否异常，异常则触发通知推送、TTS语音播报、家属端记录
    public static void checkAndAlert(Context context, HealthData data) {
        EvaluationResult result = HealthMetricEvaluator.evaluate(data.type, data.value);
        if (result.alertLevel == AlertLevel.NORMAL) return;

        String message = buildAlertMessage(data, result.alertLevel);
        Repository repo = Repository.getInstance();
        repo.addEmergencyAlert(message);
        showNotification(context, repo.getActiveUserId(), message);
        ttsAdapter.speak("健康提醒：" + message);

        List<FamilyMember> members = repo.getFamilyMembers();
        if (!members.isEmpty()) {
            String familyMsg = "已通知家属：" + members.get(0).name;
            if (members.size() > 1) familyMsg += "、" + members.get(1).name;
            repo.addEmergencyAlert(familyMsg);
        }
    }

    private static String buildAlertMessage(HealthData data, AlertLevel level) {
        String prefix = level == AlertLevel.CRITICAL ? "⚠️ 紧急" : "注意";
        return prefix + "：" + data.getLabel() + " " + data.value + " " + data.getUnit()
            + "，建议联系医生或家属。";
    }

    private static void showNotification(Context context, int userId, String message) {
        Intent intent = new Intent(context, MainActivity.class)
            .putExtra("user_id", userId)
            .putExtra("initial_tab", "health")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationRequest request = new NotificationRequest(
            userId,
            CHANNEL_ID,
            CHANNEL_NAME,
            "健康异常告警",
            message,
            R.drawable.ic_notification_health,
            intent,
            NotificationManager.IMPORTANCE_HIGH,
            true,
            alertIdCounter++);
        notificationDispatcher.notify(context, request);
    }
}