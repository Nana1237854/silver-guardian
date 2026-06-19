package com.silverguardian.prototype.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicine_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine_name");
        String userName = intent.getStringExtra("user_name");
        String message = buildReminderMessage(userName, medicineName);

        // 通知栏
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "用药提醒", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_medicine)
            .setContentTitle("用药提醒")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());

        // 确保 TTS 已初始化（进程可能已被系统回收，静态单例已丢失）
        TtsHelper.init(context);
        TtsHelper.speak(message);
    }

    // 提取为 package-visible 以便测试
    static String buildReminderMessage(String userName, String medicineName) {
        if (medicineName == null || medicineName.isEmpty()) medicineName = "药品";
        if (userName == null || userName.isEmpty()) userName = "老人家";
        return userName + "，该吃「" + medicineName + "」了，请按时服药。";
    }
}
