package com.silverguardian.prototype.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.BuildConfig;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AiChatModule {
    public interface Listener {
        void onMessagesChanged(List<ChatMessage> messages);
        void onVoiceInput(String text);
        void onVoiceListeningChanged(boolean listening);
        default void onDrugRecommendations(List<MedicineLibraryItem> drugs) { }
    }

    private static final int REQUEST_RECORD_AUDIO = 401;

    private final Context appContext;
    private final Repository repository;
    private final ContentFilter contentFilter;
    private final ReplyProvider replyProvider;
    private final ZhipuApiClient apiClient;
    private Listener listener;
    private SpeechRecognizer speechRecognizer;
    private boolean voiceEnabled = true;
    private boolean listening;

    public AiChatModule(Context context, Repository repository) {
        this.appContext = context.getApplicationContext();
        this.repository = repository;
        this.contentFilter = new ContentFilter();
        this.replyProvider = new ReplyProvider();
        this.apiClient = new ZhipuApiClient();
    }

    public void bindListener(Listener listener) {
        this.listener = listener;
    }

    public List<ChatMessage> loadMessages() {
        if (repository.getWelcomeMessages().isEmpty()) {
            repository.resetChatSession(loadSystemPromptFallback());
        }
        List<ChatMessage> messages = new ArrayList<>(repository.getWelcomeMessages());
        notifyMessagesChanged(messages);
        return messages;
    }

    public void resetConversation() {
        repository.resetChatSession(loadSystemPromptFallback());
        notifyMessagesChanged(new ArrayList<>(repository.getWelcomeMessages()));
    }

    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    public void setVoiceEnabled(boolean enabled) {
        this.voiceEnabled = enabled;
    }

    public void sendMessage(String text) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) return;

        repository.addChatMessage(clean, ChatMessage.TYPE_USER);
        notifyMessagesChanged(new ArrayList<>(repository.getWelcomeMessages()));

        String blocked = contentFilter.check(clean);
        if (blocked != null) {
            appendAiReply(blocked);
            return;
        }

        String apiKey = BuildConfig.ZHIPU_API_KEY;
        if (apiKey.startsWith("PUT_") || apiKey.length() < 10) {
            appendAiReply(replyProvider.fallback(clean));
            return;
        }

        apiClient.chat(apiKey, clean, loadSystemPrompt(), new ZhipuApiClient.Callback() {
            @Override public void onSuccess(String reply) { appendAiReply(reply); }
            @Override public void onFailure(String error) { appendAiReply(replyProvider.fallback(clean)); }
        });
    }

    public void toggleVoiceInput(AppCompatActivity activity) {
        if (listening) {
            if (speechRecognizer != null) speechRecognizer.stopListening();
            setListening(false);
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }
        ensureSpeechRecognizer(activity);
        if (speechRecognizer == null) return;
        setListening(true);
        android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    public void onPermissionResult(int requestCode, @NonNull int[] grantResults, AppCompatActivity activity) {
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleVoiceInput(activity);
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        setListening(false);
        listener = null;
    }

    private void ensureSpeechRecognizer(AppCompatActivity activity) {
        if (speechRecognizer != null) return;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { setListening(false); }
            @Override public void onEvent(int eventType, Bundle params) { }

            @Override
            public void onError(int error) {
                setListening(false);
            }

            @Override
            public void onResults(Bundle results) {
                setListening(false);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (listener != null && matches != null && !matches.isEmpty()) listener.onVoiceInput(matches.get(0));
            }

            @Override public void onPartialResults(Bundle partialResults) { }
        });
    }

    private void setListening(boolean listening) {
        this.listening = listening;
        if (listener != null) listener.onVoiceListeningChanged(listening);
    }

    private void appendAiReply(String reply) {
        DrugRecommendationParser.Result parsed = DrugRecommendationParser.parse(reply, repository.getMedicineLibrary());
        repository.addChatMessage(parsed.visibleText, ChatMessage.TYPE_AI);
        notifyMessagesChanged(new ArrayList<>(repository.getWelcomeMessages()));
        if (!parsed.drugs.isEmpty() && listener != null) listener.onDrugRecommendations(parsed.drugs);
        if (voiceEnabled) TtsHelper.speak(parsed.visibleText);
    }

    private void notifyMessagesChanged(List<ChatMessage> messages) {
        if (listener != null) listener.onMessagesChanged(messages);
    }

    private String loadSystemPrompt() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
            appContext.getResources().openRawResource(R.raw.system_prompt), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            StringBuilder context = new StringBuilder(sb);
            context.append("\n当前老人的药品库："); for (MedicineLibraryItem item : repository.getMedicineLibrary()) context.append(item.name).append("、");
            context.append("\n当前用药和今日打卡："); for (Medicine medicine : repository.getMedicines()) context.append(medicine.name).append("(").append(medicine.time).append(medicine.takenToday ? "，已打卡" : "，未打卡").append(")；");
            return context.toString();
        } catch (Exception e) {
            return loadSystemPromptFallback();
        }
    }

    private String loadSystemPromptFallback() {
        return appContext.getString(R.string.chat_system_prompt_fallback);
    }
}
