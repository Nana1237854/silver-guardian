package com.silverguardian.prototype.notification;

import android.content.Context;

// 通知发送接口：统一通知分发抽象
public interface NotificationDispatcher {
    // 分发通知：由具体平台实现通知的发送
    void notify(Context context, NotificationRequest request);
}