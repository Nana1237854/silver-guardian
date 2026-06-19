package com.silverguardian.prototype;

import android.app.Application;
import android.content.Context;

import com.silverguardian.prototype.app.AppContainer;

public class SilverGuardianApp extends Application {
    private AppContainer appContainer;

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
