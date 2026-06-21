package com.silverguardian.prototype.notification;

import android.content.Context;

public interface NotificationDispatcher {
    void notify(Context context, NotificationRequest request);
}