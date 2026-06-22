package com.silverguardian.prototype;

import android.app.Application;
import android.content.Context;

import com.silverguardian.prototype.app.AppContainer;

// Application入口：全局模块初始化
public class SilverGuardianApp extends Application {
    private AppContainer appContainer;

    // 应用启动初始化：创建依赖容器、防诈通知渠道、WorkManager周期任务
    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    public static AppContainer from(Context context) {
        return ((SilverGuardianApp) context.getApplicationContext()).getAppContainer();
    }
}
