package com.silverguardian.prototype;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.ai.ContentFilter;
import com.silverguardian.prototype.ai.ReplyProvider;
import com.silverguardian.prototype.ai.SpeechManager;
import com.silverguardian.prototype.ai.ZhipuApiClient;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends BaseActivity {

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText input;
    private RecyclerView messageList;
    private DrawerLayout drawerLayout;
    private SpeechManager speechManager;
    private ContentFilter contentFilter;
    private ReplyProvider replyProvider;
    private ZhipuApiClient apiClient;
    private String systemPrompt;
    private boolean voiceEnabled = true;
    private ImageButton voiceToggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        MockData.init(this);

        contentFilter = new ContentFilter();
        replyProvider = new ReplyProvider();
        apiClient = new ZhipuApiClient();
        systemPrompt = loadSystemPrompt();

        String title = getIntent().getStringExtra("chat_title");
        TextView titleView = findViewById(R.id.chat_title);
        titleView.setText(title == null
            ? getString(R.string.chat_default_title)
            : getString(R.string.chat_title_with_prefix, title));

        drawerLayout = findViewById(R.id.drawer_layout);
        setupToolbar();
        setupSuggestions();
        setupMessageList();
        setupInput();
        setupDrawer();
        setupVoice();

        loadWelcomeMessages();
    }

    private String loadSystemPrompt() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getResources().openRawResource(R.raw.system_prompt), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return getString(R.string.chat_system_prompt_fallback);
        }
    }

    private void setupToolbar() {
        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        voiceToggleBtn = findViewById(R.id.btn_voice_toggle);
        updateVoiceToggleState();
        voiceToggleBtn.setOnClickListener(v -> {
            voiceEnabled = !voiceEnabled;
            updateVoiceToggleState();
        });
    }

    private void updateVoiceToggleState() {
        voiceToggleBtn.setImageResource(voiceEnabled ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);
        voiceToggleBtn.setContentDescription(getString(voiceEnabled ? R.string.chat_voice_on : R.string.chat_voice_off));
    }

    private void setupSuggestions() {
        LinearLayout row = findViewById(R.id.suggestions_row);
        int[] suggestionIds = {
            R.string.chat_suggestion_1,
            R.string.chat_suggestion_2,
            R.string.chat_suggestion_3
        };
        for (int suggestionId : suggestionIds) {
            String text = getString(suggestionId);
            TextView chip = (TextView) getLayoutInflater().inflate(R.layout.item_chat_suggestion, row, false);
            chip.setText(text);
            chip.setOnClickListener(v -> input.setText(text));
            row.addView(chip);
        }
    }

    private void setupMessageList() {
        messageList = findViewById(R.id.message_list);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, messages);
        messageList.setAdapter(adapter);
    }

    private void loadWelcomeMessages() {
        messages.addAll(MockData.getWelcomeMessages());
        adapter.notifyDataSetChanged();
        if (!messages.isEmpty()) {
            messageList.scrollToPosition(messages.size() - 1);
        }
    }

    private void setupInput() {
        input = findViewById(R.id.chat_input);
        findViewById(R.id.btn_send).setOnClickListener(v -> send());
    }

    private void setupVoice() {
        ImageButton voiceButton = findViewById(R.id.btn_voice);
        speechManager = new SpeechManager(this, voiceButton, text -> {
            input.setText(text);
            input.setSelection(input.length());
        });
        voiceButton.setOnClickListener(v -> speechManager.toggle());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (speechManager != null) {
            speechManager.onPermissionResult(requestCode, grantResults);
        }
    }

    private void setupDrawer() {
        TextView newChat = findViewById(R.id.drawer_new_chat);
        newChat.setOnClickListener(v -> {
            messages.clear();
            messages.addAll(MockData.getWelcomeMessages());
            adapter.notifyDataSetChanged();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        LinearLayout recentList = findViewById(R.id.drawer_recent_list);
        int[] itemIds = {
            R.string.chat_recent_1,
            R.string.chat_recent_2,
            R.string.chat_recent_3,
            R.string.chat_recent_4,
            R.string.chat_recent_5
        };
        for (int itemId : itemIds) {
            String item = getString(itemId);
            TextView row = (TextView) getLayoutInflater().inflate(R.layout.item_chat_recent, recentList, false);
            row.setText(item);
            row.setOnClickListener(v -> {
                input.setText(item);
                input.setSelection(input.length());
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            recentList.addView(row);
        }
    }

    private void send() {
        String text = input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ChatMessage userMessage = new ChatMessage(text, ChatMessage.TYPE_USER, MockData.now());
        messages.add(userMessage);
        MockData.addChatMessage(text, ChatMessage.TYPE_USER);
        input.setText("");
        adapter.notifyItemInserted(messages.size() - 1);
        messageList.scrollToPosition(messages.size() - 1);

        String blocked = contentFilter.check(text);
        if (blocked != null) {
            appendAiReply(blocked);
            return;
        }

        String apiKey = BuildConfig.ZHIPU_API_KEY;
        if (apiKey.startsWith("PUT_") || apiKey.length() < 10) {
            appendAiReply(replyProvider.fallback(text));
            return;
        }

        apiClient.chat(apiKey, text, systemPrompt, new ZhipuApiClient.Callback() {
            @Override public void onSuccess(String reply) { appendAiReply(reply); }
            @Override public void onFailure(String error) { appendAiReply(replyProvider.fallback(text)); }
        });
    }

    private void appendAiReply(String reply) {
        messages.add(new ChatMessage(reply, ChatMessage.TYPE_AI, MockData.now()));
        MockData.addChatMessage(reply, ChatMessage.TYPE_AI);
        adapter.notifyItemInserted(messages.size() - 1);
        messageList.scrollToPosition(messages.size() - 1);

        if (voiceEnabled) {
            TtsHelper.speak(reply);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechManager != null) {
            speechManager.destroy();
        }
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}