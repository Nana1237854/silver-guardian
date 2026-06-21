package com.silverguardian.prototype;

import android.app.Application;
import android.content.Context;

import com.silverguardian.prototype.app.AppContainer;
import com.silverguardian.prototype.reminder.FraudNotificationHelper;
import com.silverguardian.prototype.reminder.FraudReminderScheduler;

public class SilverGuardianApp extends Application {
    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
        // 初始化防诈提醒通知渠道（Android 8.0+）
        FraudNotificationHelper.createChannel(this);
        // 如果用户已开启每日防诈提醒，注册 WorkManager 周期任务
        if (FraudReminderScheduler.isReminderEnabled(this)) {
            FraudReminderScheduler.scheduleDailyReminder(this);
        }
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    public static AppContainer from(Context context) {
        return ((SilverGuardianApp) context.getApplicationContext()).getAppContainer();
    }
}
