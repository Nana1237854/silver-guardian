package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * TTS 单例 — 在 MainActivity 启动时预初始化，BroadcastReceiver 直接复用。
 * 避免 BroadcastReceiver 短暂生命周期导致异步回调丢失。
 */
public class TtsHelper {
    private static TextToSpeech instance;
    private static boolean ready = false;

    public static void init(Context context) {
        if (instance != null) return;
        instance = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = instance.setLanguage(Locale.CHINESE);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TtsHelper", "TTS 中文语言包未安装");
                } else {
                    instance.setSpeechRate(0.8f);
                    ready = true;
                    Log.d("TtsHelper", "TTS 初始化成功");
                }
            } else {
                Log.e("TtsHelper", "TTS 初始化失败, status=" + status);
            }
        });
    }

    public static void speak(String message) {
        if (instance != null && ready) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med_reminder");
        }
    }

    public static void release() {
        if (instance != null) {
            instance.stop();
            instance.shutdown();
            instance = null;
            ready = false;
        }
    }
}
