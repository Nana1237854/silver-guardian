package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.silverguardian.prototype.MainActivity;

import java.util.Locale;

public class TtsHelper {
    private static final String TAG = "TtsHelper";
    private static final int MAX_RETRIES = 5;

    private static final String[] CANDIDATES = {
        "com.google.android.tts", "com.iflytek.speechcloud",
        "com.xiaomi.mibrain.speech", "com.huawei.hiai",
        "com.oppo.engine.talkback",
    };

    private static TextToSpeech instance;
    private static volatile boolean ready;
    private static String pendingMessage;
    private static Context appContext;
    private static int retryCount;

    public static void init(Context context) {
        if (instance != null) return;
        appContext = context.getApplicationContext();
        retryCount = 0;
        tryEngine(-1);
    }

    private static void tryEngine(int idx) {
        if (instance != null) { instance.shutdown(); instance = null; }
        ready = false;

        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "TTS 不可用 (已尝试 " + retryCount + " 次), 仅使用通知栏提醒");
            return;
        }
        retryCount++;

        if (idx + 1 >= CANDIDATES.length) {
            // 所有候选引擎失败，最后一次用默认引擎
            Log.d(TAG, "尝试默认引擎");
            instance = new TextToSpeech(appContext, s -> onInit(s, "default"));
            return;
        }

        String engine = CANDIDATES[idx + 1];
        Log.d(TAG, "尝试引擎: " + engine);
        instance = new TextToSpeech(appContext, s -> onInit(s, engine), engine);
    }

    private static void onInit(int status, String engine) {
        if (status == TextToSpeech.SUCCESS) {
            int r = instance.setLanguage(Locale.CHINESE);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                r = instance.setLanguage(Locale.SIMPLIFIED_CHINESE);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, engine + " 不支持中文");
                tryEngine(-1); // 继续尝试
            } else {
                instance.setSpeechRate(0.8f);
                ready = true;
                Log.d(TAG, "TTS 就绪: " + engine);
                if (pendingMessage != null) {
                    instance.speak(pendingMessage, TextToSpeech.QUEUE_FLUSH, null, "med");
                    pendingMessage = null;
                }
            }
        } else {
            Log.w(TAG, engine + " 失败 status=" + status);
            new Handler(Looper.getMainLooper()).postDelayed(() -> tryEngine(-1), 300);
        }
    }

    public static void speak(String message) {
        if (instance != null && ready) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med");
        } else if (retryCount >= MAX_RETRIES) {
            Log.w(TAG, "TTS 不可用, 跳过语音播报");
        } else {
            pendingMessage = message;
        }
    }

    public static void release() {
        if (instance != null) { instance.stop(); instance.shutdown(); instance = null; }
        ready = false; pendingMessage = null;
    }
}
