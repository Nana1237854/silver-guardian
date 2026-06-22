package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

// TextToSpeech初始化与播报工具：TTS引擎管理、语音播报调用
/**
 * TTS 单例 — 使用系统默认引擎（与系统设置中的引擎一致）。
 * 系统文字转语音测试能响 → App 就能响。
 */
public class TtsHelper {
    private static final String TAG = "TtsHelper";
    private static TextToSpeech instance;
    private static volatile boolean ready;
    private static volatile boolean initialized;
    private static String pendingMessage;

    // 初始化TTS引擎：创建TextToSpeech实例并设置中文语音
    public static void init(Context context) {
        if (instance != null) return;
        Context app = context.getApplicationContext();
        // 不指定引擎，使用用户在 设置→文字转语音 中选择的默认引擎
        instance = new TextToSpeech(app, status -> {
            initialized = true;
            if (status == TextToSpeech.SUCCESS) {
                int r = instance.setLanguage(Locale.CHINESE);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                    r = instance.setLanguage(Locale.SIMPLIFIED_CHINESE);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "默认引擎不支持中文, 请到系统设置安装中文语音数据");
                } else {
                    instance.setSpeechRate(0.8f);
                    ready = true;
                    Log.d(TAG, "TTS 就绪, 引擎=" + instance.getDefaultEngine());
                    if (pendingMessage != null) {
                        instance.speak(pendingMessage, TextToSpeech.QUEUE_FLUSH, null, "med");
                        pendingMessage = null;
                    }
                }
            } else {
                Log.e(TAG, "TTS 初始化失败 status=" + status);
            }
        });
    }

    // 语音播报：将文字转换为语音输出
    public static void speak(String message) {
        if (instance != null && ready) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med");
            Log.d(TAG, "speak: " + message);
        } else if (instance != null && !initialized) {
            pendingMessage = message;
        } else if (instance != null) {
            Log.e(TAG, "TTS 不可用，消息丢弃");
        } else {
            Log.w(TAG, "TTS 未初始化");
        }
    }

    // 停止当前语音播报
    public static void stop() {
        pendingMessage = null;
        if (instance != null) {
            instance.stop();
        }
    }

    // 销毁TTS引擎：停止播报、释放资源
    public static void release() {
        if (instance != null) { instance.stop(); instance.shutdown(); instance = null; }
        ready = false; pendingMessage = null; initialized = false;
    }
}
