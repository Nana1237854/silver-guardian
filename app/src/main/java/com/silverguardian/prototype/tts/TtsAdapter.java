package com.silverguardian.prototype.tts;

public interface TtsAdapter {
    void speak(String text);
    void stop();
}