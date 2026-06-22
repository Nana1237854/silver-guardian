package com.silverguardian.prototype.tts;

// TTS语音播报接口：文字转语音抽象
public interface TtsAdapter {
    // 播报指定文本
    void speak(String text);
    // 停止当前播报
    void stop();
}