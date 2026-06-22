package com.silverguardian.prototype.reminder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.notification.AndroidNotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationRequest;
import com.silverguardian.prototype.tts.AndroidTtsAdapter;
import com.silverguardian.prototype.tts.TtsAdapter;

// 用药提醒广播接收器：接收闹钟广播、发送通知、TTS语音播报、重复提醒
public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicine_reminder";
    private static final NotificationDispatcher notificationDispatcher =
        new AndroidNotificationDispatcher();
    private static final TtsAdapter ttsAdapter = new AndroidTtsAdapter();

    @Override
    // 接收闹钟广播：发送通知、TTS语音播报、处理重复提醒逻辑
    public void onReceive(Context context, Intent intent) {
        int userId = intent.getIntExtra("user_id", -1);
        int medicineId = intent.getIntExtra("medicine_id", -1);
        Repository repo = Repository.init(context);
        if (userId > 0 && medicineId > 0
                && repo.isMedicineTakenToday(userId, medicineId)) {
            return;
        }

        String medicineName = intent.getStringExtra("medicine_name");
        String userName = intent.getStringExtra("user_name");
        String message = buildReminderMessage(userName, medicineName);
        notify(context, message, userId);
        TtsHelper.init(context);
        ttsAdapter.speak(message);

        int repeatIndex = intent.getIntExtra("repeat_index", 0);
        int repeatCount = intent.getIntExtra("repeat_count", 0);
        if (repeatIndex < repeatCount) {
            Medicine medicine = new Medicine(medicineId, medicineName, "", "", "", "", "",
                false, 0, repeatCount, intent.getIntExtra("repeat_interval", 10));
            MedicineAlarmScheduler.scheduleAt(context, userId, userName, medicine,
                intent.getIntExtra("slot", 0), repeatIndex + 1,
                System.currentTimeMillis() + medicine.repeatInterval * 60000L);
        }
    }

    private void notify(Context context, String message, int userId) {
        Intent targetIntent = new Intent(context, MainActivity.class)
            .putExtra("user_id", userId)
            .putExtra("initial_tab", "medicine")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationRequest request = new NotificationRequest(
            userId,
            CHANNEL_ID,
            "用药提醒",
            "用药提醒",
            message,
            R.drawable.ic_notification_medicine,
            targetIntent,
            NotificationManager.IMPORTANCE_HIGH,
            true,
            0);
        notificationDispatcher.notify(context, request);
    }

    static String buildReminderMessage(String user, String medicine) {
        if (medicine == null || medicine.isEmpty()) medicine = "药品";
        if (user == null || user.isEmpty()) user = "老人家";
        return user + "，该吃「" + medicine + "」了，请按时服药。";
    }
}