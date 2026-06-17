package com.silverguardian.prototype.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        TtsHelper.init(context);  // 开机后预初始化TTS

        // 重启后恢复所有用药闹钟
        try {
            SQLiteDatabase db = context.openOrCreateDatabase("elderly_guardian.db", Context.MODE_PRIVATE, null);
            Cursor c = db.rawQuery("SELECT u.name, m.name, m.time FROM user_medicines m JOIN users u ON u.id=m.user_id", null);
            while (c.moveToNext()) {
                String userName = c.getString(0);
                String medName = c.getString(1);
                String times = c.getString(2);
                if (times == null) continue;
                for (String time : times.split(",")) {
                    String[] parts = time.trim().split(":");
                    if (parts.length != 2) continue;
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    scheduleAlarm(context, userName, medName, hour, minute);
                }
            }
            c.close();
            db.close();
        } catch (Exception ignored) {
            // DB not yet initialized — ok, will be set when user adds medicines
        }
    }

    private void scheduleAlarm(Context context, String userName, String medName, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ReminderBroadcastReceiver.class);
        i.putExtra("medicine_name", medName);
        i.putExtra("user_name", userName);
        PendingIntent pi = PendingIntent.getBroadcast(context, (userName + medName + hour + ":" + minute).hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        if (cal.before(java.util.Calendar.getInstance())) cal.add(java.util.Calendar.DAY_OF_MONTH, 1);

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
}
