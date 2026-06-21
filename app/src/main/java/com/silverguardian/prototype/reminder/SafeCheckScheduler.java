package com.silverguardian.prototype.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.User;

import java.util.Calendar;
import java.util.Objects;

public final class SafeCheckScheduler {
    private static final int REMINDER_HOUR = 9;
    private static final int FOLLOW_UP_HOUR = 12;
    private static final int MISSED_HOUR = 15;

    private SafeCheckScheduler() {
    }

    public static void scheduleForAll(Context context, Repository repository) {
        for (User user : repository.getUsers()) {
            scheduleDaily(context, user.id);
        }
    }

    public static void scheduleDaily(Context context, int userId) {
        scheduleAction(context, userId, SafeCheckReceiver.ACTION_REMINDER, nextAt(REMINDER_HOUR, 0));
        scheduleAction(context, userId, SafeCheckReceiver.ACTION_FOLLOW_UP, nextAt(FOLLOW_UP_HOUR, 0));
        scheduleAction(context, userId, SafeCheckReceiver.ACTION_MISSED, nextAt(MISSED_HOUR, 0));
    }

    public static void scheduleDelayedReminder(Context context, int userId, int delayMinutes) {
        scheduleAction(context, userId, SafeCheckReceiver.ACTION_DELAYED,
            System.currentTimeMillis() + delayMinutes * 60_000L);
    }

    private static void scheduleAction(Context context, int userId, String action, long triggerAtMillis) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) {
            return;
        }
        PendingIntent operation = pendingIntent(context, userId, action, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canScheduleExact(alarms)) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else {
                alarms.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            }
        } catch (SecurityException ignored) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            } else {
                alarms.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            }
        }
    }

    private static PendingIntent pendingIntent(Context context, int userId, String action, int flags) {
        Intent intent = new Intent(context, SafeCheckReceiver.class)
            .setAction(action)
            .putExtra("user_id", userId);
        return PendingIntent.getBroadcast(
            context,
            requestCode(userId, action),
            intent,
            flags | PendingIntent.FLAG_IMMUTABLE);
    }

    private static int requestCode(int userId, String action) {
        return Objects.hash(userId, action);
    }

    private static long nextAt(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (!calendar.after(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTimeInMillis();
    }

    private static boolean canScheduleExact(AlarmManager alarms) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms();
    }
}
