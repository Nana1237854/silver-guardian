package com.silverguardian.prototype.notification;

import android.content.Intent;

public final class NotificationRequest {
    public final int targetUserId;
    public final String channelId;
    public final String channelName;
    public final String title;
    public final String message;
    public final int smallIconRes;
    public final Intent targetIntent;
    public final int importance;
    public final boolean autoCancel;
    public final int notificationId;

    public NotificationRequest(int targetUserId, String channelId, String channelName,
            String title, String message, int smallIconRes, Intent targetIntent,
            int importance, boolean autoCancel, int notificationId) {
        this.targetUserId = targetUserId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.title = title;
        this.message = message;
        this.smallIconRes = smallIconRes;
        this.targetIntent = targetIntent;
        this.importance = importance;
        this.autoCancel = autoCancel;
        this.notificationId = notificationId;
    }
}