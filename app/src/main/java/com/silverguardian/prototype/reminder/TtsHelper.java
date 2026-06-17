package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * TTS 单例 — 预初始化 + 自动重试 + 消息缓存。
 */
public class TtsHelper {
    private static final String TAG = "TtsHelper";
    private static TextToSpeech instance;
    private static volatile boolean ready;
    private static String pendingMessage;
    private static int retryCount;

    public static void init(Context context) {
        if (instance != null) return;
        createEngine(context.getApplicationContext());
    }

    private static void createEngine(Context appContext) {
        ready = false;
        instance = new TextToSpeech(appContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int r = instance.setLanguage(Locale.CHINESE);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                    r = instance.setLanguage(Locale.SIMPLIFIED_CHINESE);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "TTS 中文不可用，请到设置→语言→文字转语音安装中文语音数据");
                } else {
                    instance.setSpeechRate(0.8f);
                    instance.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override public void onStart(String s) { Log.d(TAG, "TTS 开始播报"); }
                        @Override public void onDone(String s) { Log.d(TAG, "TTS 播报完成"); }
                        @Override public void onError(String s) { Log.e(TAG, "TTS 播报出错: " + s); }
                    });
                    ready = true;
                    retryCount = 0;
                    Log.d(TAG, "TTS 就绪");
                    if (pendingMessage != null) {
                        instance.speak(pendingMessage, TextToSpeech.QUEUE_FLUSH, null, "med");
                        pendingMessage = null;
                    }
                }
            } else {
                Log.e(TAG, "TTS 初始化失败 status=" + status);
                if (retryCount < 2) {
                    retryCount++;
                    if (instance != null) { instance.shutdown(); instance = null; }
                    new Handler(Looper.getMainLooper()).postDelayed(() -> createEngine(appContext), 2000);
                }
            }
        });
    }

    public static void speak(String message) {
        if (instance != null && ready) {
            instance.speak(message, TextToSpeech.QUEUE_FLUSH, null, "med");
            Log.d(TAG, "speak: " + message);
        } else if (instance != null) {
            pendingMessage = message;
            Log.d(TAG, "缓存消息等待TTS就绪");
        } else {
            Log.w(TAG, "TTS实例为空");
        }
    }

    public static void release() {
        if (instance != null) { instance.stop(); instance.shutdown(); instance = null; }
        ready = false; pendingMessage = null;
    }
}
