package com.silverguardian.prototype.modules;

import android.content.Context;
import android.content.SharedPreferences;

public class SeniorGuideModule {
    private static final String PREFS_NAME = "senior_guides";

    public static final String GUIDE_HOME = "guide_home_shown";
    public static final String GUIDE_MEDICINE = "guide_medicine_shown";
    public static final String GUIDE_FAMILY_MANAGE = "guide_family_manage_shown";
    public static final String GUIDE_ALBUM = "guide_album_shown";
    public static final String GUIDE_FRAUD = "guide_fraud_shown";
    public static final String GUIDE_SAFE_CHECK = "guide_safe_check_shown";
    public static final String GUIDE_SOS = "guide_sos_shown";
    public static final String GUIDE_CARE_SUMMARY = "guide_care_summary_shown";
    public static final String GUIDE_MEDICINE_FEEDBACK = "guide_medicine_feedback_shown";

    private final SharedPreferences prefs;

    public SeniorGuideModule(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 判断指定页面是否需要展示引导。
     * 首次进入返回 true，已标记展示后返回 false。
     */
    public boolean shouldShow(String key) {
        if (key == null) {
            return false;
        }
        return !prefs.getBoolean(key, false);
    }

    /**
     * 标记指定页面引导已展示，后续进入不再展示。
     */
    public void markShown(String key) {
        if (key == null) {
            return;
        }
        prefs.edit().putBoolean(key, true).apply();
    }

    /**
     * 查询指定页面引导是否已展示。
     */
    public boolean hasShown(String key) {
        if (key == null) {
            return false;
        }
        return prefs.getBoolean(key, false);
    }

    /**
     * 重置单个页面的引导状态，下次进入该页面将重新展示引导。
     */
    public void reset(String key) {
        if (key == null) {
            return;
        }
        prefs.edit().remove(key).apply();
    }

    /**
     * 重置全部引导状态，所有页面下次进入都将重新展示引导。
     */
    public void resetAll() {
        prefs.edit().clear().apply();
    }
}
