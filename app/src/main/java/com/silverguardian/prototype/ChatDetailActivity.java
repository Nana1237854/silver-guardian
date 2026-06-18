package com.silverguardian.prototype;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class ChatDetailActivity extends AppCompatActivity {

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
        if (title != null) {
            TextView titleView = findViewById(R.id.chat_title);
            titleView.setText("●  " + title);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        setupToolbar();
        setupSuggestions();
        setupMessageList();
        setupInput();
        setupDrawer();
        setupVoice();

        loadWelcomeMessages();
    }

    // ========== System Prompt ==========

    private String loadSystemPrompt() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getResources().openRawResource(R.raw.system_prompt), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        } catch (Exception e) {
            return "你是银发守护者智能助手，专门为老年人提供健康管理、用药提醒、生活辅助等服务。";
        }
    }

    // ========== Toolbar ==========

    private void setupToolbar() {
        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(Gravity.START));
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        voiceToggleBtn = findViewById(R.id.btn_voice_toggle);
        voiceToggleBtn.setOnClickListener(v -> {
            voiceEnabled = !voiceEnabled;
            voiceToggleBtn.setImageResource(voiceEnabled
                ? android.R.drawable.ic_lock_silent_mode_off
                : android.R.drawable.ic_lock_silent_mode);
            voiceToggleBtn.setContentDescription(voiceEnabled ? "语音播报已开启" : "语音播报已关闭");
        });
    }

    // ========== Suggestions ==========

    private void setupSuggestions() {
        LinearLayout row = findViewById(R.id.suggestions_row);
        String[] suggestions = {
            "我今天血压有点高，怎么办？",
            "适合长辈的简单运动有哪些？",
            "晚上睡不好，如何改善？"
        };
        for (String text : suggestions) {
            TextView chip = new TextView(this);
            chip.setText(text);
            chip.setTextSize(14);
            chip.setTextColor(getColor(R.color.text_primary));
            chip.setBackgroundResource(R.drawable.bg_chip_soft);
            chip.setPadding(dp(18), dp(12), dp(18), dp(12));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(48));
            params.rightMargin = dp(8);
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> input.setText(text));
            row.addView(chip);
        }
    }

    // ========== Message List ==========

    private void setupMessageList() {
        messageList = findViewById(R.id.message_list);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, messages);
        messageList.setAdapter(adapter);
    }

    private void loadWelcomeMessages() {
        messages.addAll(MockData.getWelcomeMessages());
        adapter.notifyDataSetChanged();
        if (!messages.isEmpty()) messageList.scrollToPosition(messages.size() - 1);
    }

    // ========== Input ==========

    private void setupInput() {
        input = findViewById(R.id.chat_input);
        findViewById(R.id.btn_send).setOnClickListener(v -> send());
    }

    // ========== Voice ==========

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
        if (speechManager != null) speechManager.onPermissionResult(requestCode, grantResults);
    }

    // ========== Drawer ==========

    private void setupDrawer() {
        TextView newChat = findViewById(R.id.drawer_new_chat);
        newChat.setOnClickListener(v -> {
            messages.clear();
            messages.addAll(MockData.getWelcomeMessages());
            adapter.notifyDataSetChanged();
            drawerLayout.closeDrawer(Gravity.START);
        });

        LinearLayout recentList = findViewById(R.id.drawer_recent_list);
        String[] items = {"血压偏高怎么办？", "推荐适合的早餐", "用药时间提醒设定", "改善睡眠的方法", "适合我的运动建议"};
        for (String item : items) {
            TextView row = new TextView(this);
            row.setText(item);
            row.setTextSize(17);
            row.setTextColor(getColor(R.color.text_primary));
            row.setPadding(0, dp(18), 0, dp(18));
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setOnClickListener(v -> {
                input.setText(item);
                input.setSelection(input.length());
                drawerLayout.closeDrawer(Gravity.START);
            });
            recentList.addView(row);
        }
    }

    // ========== Send ==========

    private void send() {
        String text = input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage userMessage = new ChatMessage(text, ChatMessage.TYPE_USER, MockData.now());
        messages.add(userMessage);
        MockData.addChatMessage(text, ChatMessage.TYPE_USER);
        input.setText("");
        adapter.notifyItemInserted(messages.size() - 1);
        messageList.scrollToPosition(messages.size() - 1);

        // 前置过滤
        String blocked = contentFilter.check(text);
        if (blocked != null) {
            appendAiReply(blocked);
            return;
        }

        // 智谱 API 调用
        String apiKey = getString(R.string.zhipu_api_key);
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

    // ========== Lifecycle ==========

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechManager != null) speechManager.destroy();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
