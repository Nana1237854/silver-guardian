package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * TTS 单例 — 逐个尝试多个引擎直到成功初始化中文TTS。
 */
public class TtsHelper {
    private static final String TAG = "TtsHelper";

    // 按优先级排列的候选引擎包名
    private static final String[] ENGINE_CANDIDATES = {
        "com.google.android.tts",           // Google TTS
        "com.iflytek.speechcloud",           // 科大讯飞
        "com.xiaomi.mibrain.speech",         // 小米小爱
        "com.huawei.hiai",                   // 华为智慧语音
        "com.oppo.engine.talkback",          // OPPO
        "com.android.providers.settings",    // 系统默认（会失败但兜底）
    };

    private static TextToSpeech instance;
    private static volatile boolean ready;
    private static String pendingMessage;
    private static Context appContext;
    private static int engineIndex;

    public static void init(Context context) {
        if (instance != null) return;
        appContext = context.getApplicationContext();
        engineIndex = -1;
        tryNextEngine();
    }

    private static void tryNextEngine() {
        if (instance != null) { instance.shutdown(); instance = null; }
        ready = false;
        engineIndex++;

        if (engineIndex >= ENGINE_CANDIDATES.length) {
            // 所有候选引擎都失败了，回退到默认引擎
            Log.e(TAG, "所有候选引擎均失败, 使用默认引擎最后尝试");
            instance = new TextToSpeech(appContext, status -> onInit(status, "default"));
            return;
        }

        String engine = ENGINE_CANDIDATES[engineIndex];
        Log.d(TAG, "尝试引擎[" + engineIndex + "]: " + engine);
        instance = new TextToSpeech(appContext, status -> onInit(status, engine), engine);
    }

    private static void onInit(int status, String engine) {
        if (status == TextToSpeech.SUCCESS) {
            int r = instance.setLanguage(Locale.CHINESE);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                r = instance.setLanguage(Locale.SIMPLIFIED_CHINESE);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, engine + " 不支持中文, 尝试下一个");
                tryNextEngine();
            } else {
                instance.setSpeechRate(0.8f);
                ready = true;
                Log.d(TAG, "TTS 成功: " + engine + ", 语言=" + instance.getLanguage());
                if (pendingMessage != null) {
                    instance.speak(pendingMessage, TextToSpeech.QUEUE_FLUSH, null, "med");
                    pendingMessage = null;
                }
            }
        } else {
            Log.w(TAG, engine + " 初始化失败 status=" + status + ", 尝试下一个");
            new Handler(Looper.getMainLooper()).postDelayed(TtsHelper::tryNextEngine, 500);
        }
    }

    public static void speak(String message) {
        if (instance != null && ready) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med");
        } else if (instance != null) {
            pendingMessage = message;
        }
    }

    public static void release() {
        if (instance != null) { instance.stop(); instance.shutdown(); instance = null; }
        ready = false; pendingMessage = null;
    }
}
