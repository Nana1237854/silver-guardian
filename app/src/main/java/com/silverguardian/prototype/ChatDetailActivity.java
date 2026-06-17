package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 401;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText input;
    private SpeechRecognizer speechRecognizer;
    private Button voiceButton;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        String title = getIntent().getStringExtra("chat_title");
        if (title == null) title = "银发守护助手";

        DrawerLayout drawer = new DrawerLayout(this);
        drawer.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(getColor(R.color.surface_white));
        drawer.addView(main, new DrawerLayout.LayoutParams(-1, -1));

        LinearLayout side = drawerPanel();
        DrawerLayout.LayoutParams drawerParams = new DrawerLayout.LayoutParams(dp(280), -1);
        drawerParams.gravity = Gravity.START;
        drawer.addView(side, drawerParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(18), dp(18), dp(18), dp(8));
        Button menu = pillButton("☰");
        top.addView(menu, new LinearLayout.LayoutParams(dp(52), dp(52)));

        TextView chip = new TextView(this);
        chip.setText(title);
        chip.setTextSize(18);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackgroundResource(R.drawable.bg_chip_soft);
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, dp(52), 1);
        chipParams.leftMargin = dp(12);
        chipParams.rightMargin = dp(12);
        top.addView(chip, chipParams);

        Button close = pillButton("✎");
        top.addView(close, new LinearLayout.LayoutParams(dp(52), dp(52)));
        main.addView(top);

        TextView avatar = new TextView(this);
        avatar.setText("●●");
        avatar.setTextSize(34);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextColor(getColor(R.color.primary));
        main.addView(avatar, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView intro = new TextView(this);
        intro.setText("你好！我是\n银发守护助手");
        intro.setGravity(Gravity.CENTER);
        intro.setTextSize(24);
        intro.setTypeface(null, android.graphics.Typeface.BOLD);
        intro.setTextColor(getColor(R.color.text_primary));
        main.addView(intro);

        TextView sub = new TextView(this);
        sub.setText("有健康问题、用药疑问或生活困扰，随时告诉我。");
        sub.setGravity(Gravity.CENTER);
        sub.setTextSize(15);
        sub.setTextColor(getColor(R.color.text_secondary));
        sub.setPadding(dp(36), dp(10), dp(36), dp(12));
        main.addView(sub);

        RecyclerView list = new RecyclerView(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        list.setAdapter(adapter);
        main.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        HorizontalScrollView suggestions = new HorizontalScrollView(this);
        suggestions.setHorizontalScrollBarEnabled(false);
        LinearLayout suggestionRow = new LinearLayout(this);
        suggestionRow.setOrientation(LinearLayout.VERTICAL);
        suggestionRow.setPadding(dp(16), dp(8), dp(16), dp(4));
        suggestionRow.addView(suggestionChip("我今天血压有点高，怎么办？"));
        suggestionRow.addView(suggestionChip("适合长辈的简单运动有哪些？"));
        suggestionRow.addView(suggestionChip("晚上睡不好，如何改善？"));
        suggestions.addView(suggestionRow);
        main.addView(suggestions);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setPadding(dp(16), dp(10), dp(16), dp(20));
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        input = new EditText(this);
        input.setHint("询问任何健康问题...");
        input.setTextSize(17);
        input.setBackgroundResource(R.drawable.bg_chat_input);
        input.setPadding(dp(18), dp(14), dp(18), dp(14));
        inputRow.addView(input, new LinearLayout.LayoutParams(0, dp(56), 1));

        voiceButton = pillButton("🎙");
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        voiceParams.leftMargin = dp(10);
        inputRow.addView(voiceButton, voiceParams);

        Button send = pillButton("➜");
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        sendParams.leftMargin = dp(8);
        inputRow.addView(send, sendParams);
        main.addView(inputRow);

        setContentView(drawer);
        messages.addAll(MockData.getWelcomeMessages());
        adapter.notifyDataSetChanged();

        // 初始化语音识别
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener);

        menu.setOnClickListener(v -> drawer.openDrawer(Gravity.START));
        close.setOnClickListener(v -> finish());
        voiceButton.setOnClickListener(v -> startVoiceInput());
        send.setOnClickListener(v -> send());
    }

    // ========== 语音输入 ==========

    private void startVoiceInput() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            voiceButton.setText("🎙");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }
        beginListening();
    }

    private void beginListening() {
        isListening = true;
        voiceButton.setText("⏹");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    private final RecognitionListener recognitionListener = new RecognitionListener() {
        @Override public void onReadyForSpeech(Bundle params) {
            Toast.makeText(ChatDetailActivity.this, "请说话...", Toast.LENGTH_SHORT).show();
        }
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            isListening = false;
            voiceButton.setText("🎙");
        }

        @Override
        public void onError(int error) {
            isListening = false;
            voiceButton.setText("🎙");
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
            Toast.makeText(ChatDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            isListening = false;
            voiceButton.setText("🎙");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                input.setText(matches.get(0));
                input.setSelection(input.getText().length());
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                input.setText(matches.get(0));
                input.setSelection(input.getText().length());
            }
        }

        @Override public void onEvent(int eventType, Bundle params) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginListening();
            } else {
                Toast.makeText(this, "需要麦克风权限才能使用语音输入", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    // ========== 侧边栏 ==========

    private LinearLayout drawerPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(26), dp(18), dp(18));
        panel.setBackgroundColor(0xFFFFFFFF);

        TextView title = new TextView(this);
        title.setText("最近对话");
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        panel.addView(title);

        String[] items = {"血压偏高怎么办？", "推荐适合的早餐", "用药时间提醒设定", "改善睡眠的方法", "适合我的运动建议"};
        for (String item : items) {
            TextView row = new TextView(this);
            row.setText(item);
            row.setTextSize(16);
            row.setTextColor(getColor(R.color.text_primary));
            row.setPadding(0, dp(18), 0, dp(18));
            panel.addView(row);
        }
        return panel;
    }

    private TextView suggestionChip(String text) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(15);
        chip.setTextColor(getColor(R.color.text_primary));
        chip.setBackgroundResource(R.drawable.bg_chip_soft);
        chip.setPadding(dp(18), dp(12), dp(18), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(8);
        chip.setLayoutParams(params);
        chip.setOnClickListener(v -> input.setText(text));
        return chip;
    }

    private Button pillButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_chip_soft);
        return button;
    }

    private void send() {
        String text = input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        ChatMessage userMessage = new ChatMessage(text, ChatMessage.TYPE_USER, MockData.now());
        messages.add(userMessage);
        MockData.addChatMessage(text, ChatMessage.TYPE_USER);
        input.setText("");
        adapter.notifyItemInserted(messages.size() - 1);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String reply = generateReply(text);
            messages.add(new ChatMessage(reply, ChatMessage.TYPE_AI, MockData.now()));
            MockData.addChatMessage(reply, ChatMessage.TYPE_AI);
            adapter.notifyItemInserted(messages.size() - 1);
        }, 500);
    }

    private String generateReply(String text) {
        if (text.contains("血压") || text.contains("高血压")) {
            return "建议今天先坐下休息 5 分钟后复测。若多次高于 140/90，请联系家属或医生。硝苯地平、缬沙坦等降压药需要遵医嘱使用，我已帮你加入用药提醒候选。";
        }
        if (text.contains("睡")) {
            return "今晚可以提前 20 分钟放下手机，睡前做 3 分钟慢呼吸。下午后少喝浓茶和咖啡，若连续失眠超过一周建议咨询医生。";
        }
        if (text.contains("运动")) {
            return "更推荐饭后慢走 20-30 分钟、扶椅抬腿和肩颈伸展。避免搬重物或一次走太远，运动时带上手机并留意胸闷、头晕。";
        }
        return "收到。我会结合你的健康档案、用药提醒和家属信息，给出温和且安全的建议。若症状明显或持续不适，请优先联系医生或家人。";
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {
        @Override public ChatHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(16), dp(8), dp(16), dp(8));
            return new ChatHolder(row);
        }
        @Override public void onBindViewHolder(ChatHolder holder, int position) { holder.bind(messages.get(position)); }
        @Override public int getItemCount() { return messages.size(); }
    }

    private class ChatHolder extends RecyclerView.ViewHolder {
        private final LinearLayout container;
        private final TextView bubble;
        private final TextView time;

        ChatHolder(LinearLayout itemView) {
            super(itemView);
            container = itemView;
            bubble = new TextView(itemView.getContext());
            bubble.setTextSize(16);
            bubble.setLineSpacing(dp(4), 1f);
            bubble.setPadding(dp(16), dp(12), dp(16), dp(12));
            time = new TextView(itemView.getContext());
            time.setTextSize(12);
            time.setTextColor(getColor(R.color.text_secondary));
            container.addView(bubble);
            container.addView(time);
        }

        void bind(ChatMessage msg) {
            container.setGravity(msg.isUser() ? Gravity.END : Gravity.START);
            bubble.setText(msg.content);
            bubble.setTextColor(getColor(R.color.text_primary));
            bubble.setBackgroundResource(msg.isUser() ? R.drawable.bg_chat_user_bubble : R.drawable.bg_chat_ai_bubble);
            time.setText(msg.time);
        }
    }
}
