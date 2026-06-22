package com.silverguardian.prototype;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.ai.AiChatModule;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.util.ArrayList;
import java.util.List;

// AI对话页面：消息列表、发送按钮、语音输入、TTS播报开关
public class ChatDetailActivity extends BaseActivity implements AiChatModule.Listener {
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText input;
    private RecyclerView messageList;
    private DrawerLayout drawerLayout;
    private ImageButton voiceButton;
    private ImageButton voiceToggleBtn;

    // 初始化AI对话界面：消息列表、输入框、语音按钮、快捷提示
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        String title = getIntent().getStringExtra("chat_title");
        TextView titleView = findViewById(R.id.chat_title);
        titleView.setText(title == null ? getString(R.string.chat_default_title) : getString(R.string.chat_title_with_prefix, title));

        drawerLayout = findViewById(R.id.drawer_layout);
        setupToolbar();
        setupSuggestions();
        setupMessageList();
        setupInput();
        setupDrawer();
        setupVoice();

        aiChat().bindListener(this);
        onMessagesChanged(aiChat().loadMessages());
    }

    private void setupToolbar() {
        findViewById(R.id.btn_menu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        voiceToggleBtn = findViewById(R.id.btn_voice_toggle);
        updateVoiceToggleState();
        voiceToggleBtn.setOnClickListener(v -> {
            boolean enableVoice = !aiChat().isVoiceEnabled();
            if (!enableVoice) {
                TtsHelper.stop();
            }
            aiChat().setVoiceEnabled(enableVoice);
            updateVoiceToggleState();
        });
    }

    private void updateVoiceToggleState() {
        boolean voiceEnabled = aiChat().isVoiceEnabled();
        voiceToggleBtn.setImageResource(voiceEnabled ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);
        voiceToggleBtn.setContentDescription(getString(voiceEnabled ? R.string.chat_voice_on : R.string.chat_voice_off));
    }

    private void setupSuggestions() {
        LinearLayout row = findViewById(R.id.suggestions_row);
        int[] suggestionIds = { R.string.chat_suggestion_1, R.string.chat_suggestion_2, R.string.chat_suggestion_3 };
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

    private void setupInput() {
        input = findViewById(R.id.chat_input);
        findViewById(R.id.btn_send).setOnClickListener(v -> send());
    }

    // 语音输入按钮：长按说话、松开发送
    private void setupVoice() {
        voiceButton = findViewById(R.id.btn_voice);
        voiceButton.setOnTouchListener((v, event) -> { if (event.getAction() == android.view.MotionEvent.ACTION_DOWN || event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) aiChat().toggleVoiceInput(this); return true; });
    }

    // 侧边栏：新对话、近期问题快捷输入
    private void setupDrawer() {
        TextView newChat = findViewById(R.id.drawer_new_chat);
        newChat.setOnClickListener(v -> {
            aiChat().resetConversation();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        LinearLayout recentList = findViewById(R.id.drawer_recent_list);
        int[] itemIds = { R.string.chat_recent_1, R.string.chat_recent_2, R.string.chat_recent_3, R.string.chat_recent_4, R.string.chat_recent_5 };
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

    // AI发送消息主流程：用户输入→网络请求→AI回复展示
    private void send() {
        String text = input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        aiChat().sendMessage(text);
        input.setText("");
    }

    @Override
    public void onMessagesChanged(List<ChatMessage> updatedMessages) {
        messages.clear();
        messages.addAll(updatedMessages);
        if (adapter != null) adapter.notifyDataSetChanged();
        if (messageList != null && !messages.isEmpty()) messageList.scrollToPosition(messages.size() - 1);
    }

    @Override
    public void onDrugRecommendations(List<com.silverguardian.prototype.models.MedicineLibraryItem> drugs) {
        runOnUiThread(() -> {
            StringBuilder names = new StringBuilder();
            for (com.silverguardian.prototype.models.MedicineLibraryItem drug : drugs) names.append("• ").append(drug.name).append("\n");
            android.widget.EditText time = new android.widget.EditText(this);
            time.setHint("服用时间，例如 08:00");
            time.setText("08:00");
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("AI 推荐药品")
                .setMessage(names + "\n请确认服用时间，保存后将加入药品库和用药提醒。")
                .setView(time)
                .setPositiveButton("加入", (d, w) -> {
                    String value = time.getText().toString().trim().isEmpty() ? "08:00" : time.getText().toString().trim();
                    for (com.silverguardian.prototype.models.MedicineLibraryItem drug : drugs) {
                        medicineReminders().addLibraryItem(drug);
                        medicineReminders().addMedicine(drug.name, drug.type, value, "口服", drug.description, 0, 0, 10);
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
        });
    }
    @Override
    public void onVoiceInput(String text) { input.setText(text); input.setSelection(input.length()); send(); }

    @Override
    public void onVoiceListeningChanged(boolean listening) {
        if (voiceButton != null) {
            voiceButton.setImageResource(listening ? R.drawable.ic_pause : R.drawable.ic_mic);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        aiChat().onPermissionResult(requestCode, grantResults, this);
    }

    @Override
    protected void onStop() {
        TtsHelper.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        aiChat().destroy();
    }
}
