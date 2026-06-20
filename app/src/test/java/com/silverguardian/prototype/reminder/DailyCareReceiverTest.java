package com.silverguardian.prototype.reminder;

import static org.junit.Assert.assertEquals;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

import org.junit.Test;

public class DailyCareReceiverTest {

    @Test
    public void api31WithoutExactPermissionUsesInexactAlarm() {
        RecordingAlarms alarms = new RecordingAlarms(false);

        DailyCareReceiver.scheduleDailyCareAlarm(alarms, Build.VERSION_CODES.UPSIDE_DOWN_CAKE, false, 1234L, null);

        assertEquals("inexact_idle", alarms.lastCall);
    }

    @Test
    public void securityExceptionFromExactFallsBackToInexactAlarm() {
        RecordingAlarms alarms = new RecordingAlarms(true);

        DailyCareReceiver.scheduleDailyCareAlarm(alarms, Build.VERSION_CODES.UPSIDE_DOWN_CAKE, true, 1234L, null);

        assertEquals("inexact_idle", alarms.lastCall);
    }

    @Test
    public void legacyApiUsesExactAlarm() {
        RecordingAlarms alarms = new RecordingAlarms(false);

        DailyCareReceiver.scheduleDailyCareAlarm(alarms, Build.VERSION_CODES.LOLLIPOP, false, 1234L, null);

        assertEquals("exact", alarms.lastCall);
    }

    private static final class RecordingAlarms implements DailyCareReceiver.AlarmCommands {
        private final boolean throwOnExactIdle;
        String lastCall = "";

        RecordingAlarms(boolean throwOnExactIdle) {
            this.throwOnExactIdle = throwOnExactIdle;
        }

        @Override
        public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
            lastCall = "exact_idle";
            if (throwOnExactIdle) {
                throw new SecurityException("missing exact alarm permission");
            }
        }

        @Override
        public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
            assertEquals(AlarmManager.RTC_WAKEUP, type);
            assertEquals(1234L, triggerAtMillis);
            lastCall = "inexact_idle";
        }

        @Override
        public void setExact(int type, long triggerAtMillis, PendingIntent operation) {
            assertEquals(AlarmManager.RTC_WAKEUP, type);
            assertEquals(1234L, triggerAtMillis);
            lastCall = "exact";
        }
    }
}