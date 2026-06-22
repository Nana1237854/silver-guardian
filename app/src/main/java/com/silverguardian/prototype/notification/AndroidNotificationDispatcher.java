package com.silverguardian.prototype.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.atomic.AtomicInteger;

// Android通知发送实现：系统NotificationManager通知发送
public final class AndroidNotificationDispatcher implements NotificationDispatcher {
    private static final AtomicInteger NEXT_NOTIFICATION_ID = new AtomicInteger(1000);

    // 通知分发实现：权限检查、渠道创建、通知构建与发送
    @Override
    public void notify(Context context, NotificationRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            createChannelIfNeeded(context, request);
            int notificationId = request.notificationId > 0
                ? request.notificationId : NEXT_NOTIFICATION_ID.incrementAndGet();
            PendingIntent contentIntent = request.targetIntent == null ? null
                : PendingIntent.getActivity(context, notificationId, request.targetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, request.channelId)
                .setSmallIcon(request.smallIconRes)
                .setContentTitle(request.title)
                .setContentText(request.message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(request.message))
                .setPriority(toCompatPriority(request.importance))
                .setAutoCancel(request.autoCancel);
            if (contentIntent != null) builder.setContentIntent(contentIntent);

            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // 通知权限可能在检查后被撤销，静默跳过，避免后台任务崩溃。
        }
    }

    private void createChannelIfNeeded(Context context, NotificationRequest request) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        manager.createNotificationChannel(new NotificationChannel(
            request.channelId, request.channelName, request.importance));
    }

    private int toCompatPriority(int importance) {
        if (importance >= NotificationManager.IMPORTANCE_HIGH) return NotificationCompat.PRIORITY_HIGH;
        if (importance <= NotificationManager.IMPORTANCE_LOW) return NotificationCompat.PRIORITY_LOW;
        return NotificationCompat.PRIORITY_DEFAULT;
    }
}