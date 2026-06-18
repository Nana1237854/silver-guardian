package com.silverguardian.prototype.ai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * 语音识别管理器。从 ChatDetailActivity 提取，管理 SpeechRecognizer 生命周期。
 */
public class SpeechManager {
    private static final int REQUEST_RECORD_AUDIO = 401;

    private final AppCompatActivity activity;
    private final ImageButton voiceButton;
    private final SpeechCallback callback;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening;

    public interface SpeechCallback {
        void onResult(String text);
    }

    public SpeechManager(AppCompatActivity activity, ImageButton voiceButton, SpeechCallback callback) {
        this.activity = activity;
        this.voiceButton = voiceButton;
        this.callback = callback;
        initRecognizer();
    }

    private void initRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Toast.makeText(activity, "请说话...", Toast.LENGTH_SHORT).show();
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                voiceButton.setImageResource(android.R.drawable.ic_btn_speak_now);
            }

            @Override
            public void onError(int error) {
                isListening = false;
                voiceButton.setImageResource(android.R.drawable.ic_btn_speak_now);
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO: msg = "录音错误"; break;
                    case SpeechRecognizer.ERROR_CLIENT: msg = "客户端错误"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: msg = "权限不足"; break;
                    case SpeechRecognizer.ERROR_NETWORK: msg = "网络错误"; break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: msg = "网络超时"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: msg = "未识别到语音，请再试一次"; break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: msg = "语音服务忙碌"; break;
                    case SpeechRecognizer.ERROR_SERVER: msg = "服务器错误"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "语音超时"; break;
                    default: msg = "识别出错（错误码 " + error + "）"; break;
                }
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                voiceButton.setImageResource(android.R.drawable.ic_btn_speak_now);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) callback.onResult(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) callback.onResult(matches.get(0));
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void toggle() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            voiceButton.setImageResource(android.R.drawable.ic_btn_speak_now);
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }
        isListening = true;
        voiceButton.setImageResource(android.R.drawable.ic_media_pause);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    public void onPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggle(); // restart listening
            } else {
                Toast.makeText(activity, "需要麦克风权限才能使用语音输入", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}
