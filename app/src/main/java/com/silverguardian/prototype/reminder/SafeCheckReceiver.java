package com.silverguardian.prototype.reminder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.SafeCheckRecord;
import com.silverguardian.prototype.notification.AndroidNotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationDispatcher;
import com.silverguardian.prototype.notification.NotificationRequest;

public class SafeCheckReceiver extends BroadcastReceiver {
    public static final String ACTION_REMINDER = "com.silverguardian.prototype.action.SAFE_CHECK_REMINDER";
    public static final String ACTION_FOLLOW_UP = "com.silverguardian.prototype.action.SAFE_CHECK_FOLLOW_UP";
    public static final String ACTION_DELAYED = "com.silverguardian.prototype.action.SAFE_CHECK_DELAYED";
    public static final String ACTION_MISSED = "com.silverguardian.prototype.action.SAFE_CHECK_MISSED";

    private static final String CHANNEL_ID = "safe_check_reminder";
    private static final NotificationDispatcher NOTIFICATIONS = new AndroidNotificationDispatcher();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        int userId = intent == null ? -1 : intent.getIntExtra("user_id", -1);
        if (userId <= 0 || action == null) {
            return;
        }

        Repository repository = Repository.init(context);
        SafeCheckRecord record = repository.getTodaySafeCheckRecord(userId);
        if (SafeCheckRecord.STATUS_OK.equals(statusOf(record))
            || SafeCheckRecord.STATUS_UNWELL.equals(statusOf(record))
            || SafeCheckRecord.STATUS_NEED_FAMILY.equals(statusOf(record))) {
            return;
        }

        if (ACTION_MISSED.equals(action)) {
            repository.saveTodaySafeCheckStatus(userId, SafeCheckRecord.STATUS_MISSED,
                context.getString(R.string.safe_check_missed_note));
            repository.addEmergencyAlertForUser(userId, context.getString(R.string.safe_check_missed_alert_message));
            sendNotification(context, userId,
                context.getString(R.string.safe_check_missed_notification_title),
                context.getString(R.string.safe_check_missed_notification_body));
            return;
        }

        int messageRes = ACTION_FOLLOW_UP.equals(action)
            ? R.string.safe_check_follow_up_body
            : R.string.safe_check_notification_body;
        sendNotification(context, userId,
            context.getString(R.string.safe_check_notification_title),
            context.getString(messageRes));
    }

    private void sendNotification(Context context, int userId, String title, String message) {
        Intent target = new Intent(context, MainActivity.class)
            .putExtra("user_id", userId)
            .putExtra("initial_tab", "home")
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationRequest request = new NotificationRequest(
            userId,
            CHANNEL_ID,
            context.getString(R.string.safe_check_notification_channel_name),
            title,
            message,
            R.drawable.ic_notifications,
            target,
            NotificationManager.IMPORTANCE_HIGH,
            true,
            820 + userId);
        NOTIFICATIONS.notify(context, request);
    }

    private String statusOf(SafeCheckRecord record) {
        return record == null ? "" : record.status;
    }
}
