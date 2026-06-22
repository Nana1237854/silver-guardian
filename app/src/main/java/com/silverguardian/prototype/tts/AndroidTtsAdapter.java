package com.silverguardian.prototype.tts;

import com.silverguardian.prototype.reminder.TtsHelper;

// TTS语音播报Android实现：TextToSpeech引擎调用
public final class AndroidTtsAdapter implements TtsAdapter {
    // 通过TtsHelper委托播报文本
    @Override
    public void speak(String text) {
        TtsHelper.speak(text);
    }

    // 通过TtsHelper委托停止播报
    @Override
    public void stop() {
        TtsHelper.stop();
    }
}