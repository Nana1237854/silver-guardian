package com.silverguardian.prototype.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import androidx.core.app.NotificationCompat;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;

import java.util.Locale;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicine_reminder";
    private static TextToSpeech tts;

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine_name");
        String userName = intent.getStringExtra("user_name");
        if (medicineName == null) medicineName = "药品";
        if (userName == null) userName = "老人家";

        String message = userName + "，该吃「" + medicineName + "」了，请按时服药。";

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
            .setSmallIcon(R.drawable.ic_medicine)
            .setContentTitle("💊 用药提醒")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());

        // TTS 语音播报
        speak(context, message);
    }

    private void speak(Context context, String message) {
        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.CHINESE);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // fallback: just notification, no TTS
                    } else {
                        tts.setSpeechRate(0.8f);
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med_reminder");
                    }
                }
            });
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med_reminder");
        }
    }
}
