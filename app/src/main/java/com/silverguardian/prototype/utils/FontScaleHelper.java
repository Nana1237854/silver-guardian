package com.silverguardian.prototype.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.R;

public class FontScaleHelper {
    private static final String PREFS = "elder_settings";
    private static final String KEY_FONT_MODE = "font_mode";
    private static final String KEY_HIGH_CONTRAST = "theme_high_contrast";

    public static int getFontModeIndex(Context context) {
        String mode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FONT_MODE, "large");
        if ("xlarge".equals(mode)) return 2;
        if ("normal".equals(mode)) return 0;
        return 1; // large default
    }

    public static void setFontMode(Context context, int index) {
        String mode = index == 0 ? "normal" : index == 2 ? "xlarge" : "large";
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_FONT_MODE, mode).apply();
    }

    public static boolean isHighContrast(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_HIGH_CONTRAST, true);
    }

    public static void setHighContrast(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }

    public static float getTextScale(Context context) {
        int mode = getFontModeIndex(context);
        if (mode == 0) return 1.0f;
        if (mode == 2) return 1.4f;
        return 1.2f; // large
    }

    public static int sp(Context context, int baseSp) {
        return Math.round(baseSp * getTextScale(context));
    }

    public static Context withAppFontScale(Context context) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.fontScale = configuration.fontScale * getTextScale(context);
        return context.createConfigurationContext(configuration);
    }

    public static int dp(Context context, int baseDp) {
        return Math.round(baseDp * context.getResources().getDisplayMetrics().density);
    }

    // 高对比度颜色切换
    public static int textPrimary(Context context) {
        return ContextCompat.getColor(context, R.color.text_primary);
    }

    public static int textSecondary(Context context) {
        return ContextCompat.getColor(context, R.color.text_secondary);
    }

    public static int bgPage(Context context) {
        return ContextCompat.getColor(context, R.color.bg_page);
    }
}
