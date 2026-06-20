package com.silverguardian.prototype.reminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.SilverGuardianApp;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.User;

import java.util.Calendar;

public class DailyCareReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "daily_care";

    @Override public void onReceive(Context context, Intent intent) {
        PendingResult pending = goAsync();
        Repository repository = Repository.init(context);
        User elder = null;
        for (User user : repository.getUsers()) if (user.id == repository.getActiveUserId()) elder = user;
        User activeElder = elder;
        SilverGuardianApp.from(context).weather().loadFromSavedLocation(report -> {
            StringBuilder medicines = new StringBuilder();
            for (Medicine medicine : repository.getMedicines()) {
                if (medicines.length() > 0) medicines.append("、");
                medicines.append(medicine.name).append("(").append(medicine.time).append(")");
            }
            String message = "早上好" + (activeElder == null ? "" : "，" + activeElder.name)
                + "！" + report.notificationSummary + "。今日用药："
                + (medicines.length() == 0 ? "暂无用药计划" : medicines) + "。";
            notify(context, message);
            TtsHelper.init(context);
            TtsHelper.speak(message);
            pending.finish();
        });
        schedule(context);
    }

    private static void notify(Context context, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            new NotificationChannel(CHANNEL, "每日关怀", NotificationManager.IMPORTANCE_HIGH));
        manager.notify(801, new NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notifications).setContentTitle("每日关怀")
            .setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build());
    }

    public static void schedule(Context context) {
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 8); next.set(Calendar.MINUTE, 0); next.set(Calendar.SECOND, 0); next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) next.add(Calendar.DAY_OF_MONTH, 1);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getBroadcast(context, 801, new Intent(context, DailyCareReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        scheduleDailyCareAlarm(new AlarmManagerCommands(alarms), Build.VERSION.SDK_INT, canScheduleExact(alarms), next.getTimeInMillis(), operation);
    }

    static boolean canScheduleExact(AlarmManager alarms) { return Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms(); }
    static void scheduleDailyCareAlarm(AlarmCommands alarms, int sdk, boolean canExact, long at, PendingIntent operation) {
        try {
            if (sdk >= 31) { if (canExact) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation); else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation); }
            else if (sdk >= 23) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation);
            else alarms.setExact(AlarmManager.RTC_WAKEUP, at, operation);
        } catch (SecurityException ignored) {
            if (sdk >= 23) alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation); else alarms.setExact(AlarmManager.RTC_WAKEUP, at, operation);
        }
    }

    interface AlarmCommands {
        void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation);
        void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation);
        void setExact(int type, long triggerAtMillis, PendingIntent operation);
    }
    static final class AlarmManagerCommands implements AlarmCommands {
        private final AlarmManager alarms;
        AlarmManagerCommands(AlarmManager alarms) { this.alarms = alarms; }
        public void setExactAndAllowWhileIdle(int type, long at, PendingIntent operation) { alarms.setExactAndAllowWhileIdle(type, at, operation); }
        public void setAndAllowWhileIdle(int type, long at, PendingIntent operation) { alarms.setAndAllowWhileIdle(type, at, operation); }
        public void setExact(int type, long at, PendingIntent operation) { alarms.setExact(type, at, operation); }
    }
}