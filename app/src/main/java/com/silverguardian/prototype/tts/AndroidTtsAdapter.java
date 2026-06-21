package com.silverguardian.prototype.tts;

import com.silverguardian.prototype.reminder.TtsHelper;

public final class AndroidTtsAdapter implements TtsAdapter {
    @Override
    public void speak(String text) {
        TtsHelper.speak(text);
    }

    @Override
    public void stop() {
        TtsHelper.stop();
    }
}