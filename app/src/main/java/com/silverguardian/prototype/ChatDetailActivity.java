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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        adapter = new ChatAdapter(this, messages);
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

        // 前置过滤：拦截明显与健康/用药/生活无关的问题
        String blocked = checkOffTopic(text);
        if (blocked != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> appendAiReply(blocked), 300);
            return;
        }

        String apiKey = getString(R.string.zhipu_api_key);
        if (apiKey.startsWith("PUT_") || apiKey.length() < 10) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                String reply = fallbackReply(text);
                appendAiReply(reply);
            }, 500);
            return;
        }
        callZhipuApi(text, apiKey);
    }

    /** 返回 null 表示允许通过；返回非 null 字符串为拦截回复 */
    private String checkOffTopic(String text) {
        String t = text.toLowerCase().trim();

        // 健康关键词（命中任一即放行）
        String[] healthKeywords = {
            "健康","病","医","药","疼","痛","痒","晕","吐","泻","咳","烧","感冒","发热",
            "血压","血糖","心率","血氧","体温","体重","体脂","睡眠","失眠","运动","锻炼",
            "饮食","吃饭","喝水","抽烟","喝酒","茶","咖啡","菜","水果","蔬菜","肉","鱼",
            "过敏","皮肤","骨头","关节","腰","腿","膝盖","心脏","肝","肾","肺","胃","肠",
            "脑","眼","耳","鼻","牙","口腔","手","脚","肩","颈","背","胸","腹",
            "老年","养老","退休","孤独","焦虑","抑郁","心情","情绪","记忆力","忘记","痴呆",
            "处方","医保","社保","挂号","检查","体检","手术","康复","护理","养生",
            "药片","胶囊","剂量","副作用","空腹","饭后","睡前","早晨","晚上",
            "散步","太极","瑜伽","广场舞","家务","买菜","超市","菜市场","药店","医院",
            "摔倒","骨折","肿","出血","伤口","急救","求助","呼叫","家人","子女","老伴",
            "头疼","头晕","胸闷","气短","心悸","乏力","没精神","食欲","胃口",
            "高血压药","降糖药","降脂药","降压药","降血糖","糖尿病药"
        };
        for (String kw : healthKeywords) {
            if (t.contains(kw)) return null; // 放行
        }

        // 明显无关的领域关键词（拦截并引导）
        String[] blockedKeywords = {
            "写代码","编程","python","java","c++","前端","后端","数据库","服务器",
            "股票","炒股","期货","基金","理财","比特币","区块链","炒币",
            "翻译","英文翻译","日文翻译","法语翻译",
            "解方程","算数","微积分","几何","代数",
            "写作文","写论文","写小说","写诗","诗歌",
            "天气","航空","高铁","地铁","公交","航班",
            "游戏","王者荣耀","吃鸡","原神","lol","dota","switch","ps5",
            "政治","选举","党派","战争",
            "汽车","房价","楼盘","房产",
            "电影","电视剧","综艺","明星","八卦",
            "法律","诉讼","合同","律师",
            "招聘","求职","面试","简历","公司",
            "减肥药","壮阳","丰胸","增高","美白针","玻尿酸"
        };
        for (String kw : blockedKeywords) {
            if (t.contains(kw)) {
                return "您问的问题和健康管理无关哦。我是您的「银发守护者」智能健康助手，" +
                       "专门帮您解答健康、用药、生活调理方面的问题。\n\n" +
                       "您可以问我：\n" +
                       "• 高血压平时要注意什么？\n" +
                       "• 降压药应该怎么吃？\n" +
                       "• 适合老人的简单运动有哪些？\n" +
                       "• 晚上睡不好怎么改善？\n" +
                       "• 糖尿病人饮食要注意什么？";
            }
        }

        // 问句很短且不匹配任何健康关键词 → 可能是在闲聊，放行给大模型判断并引导
        if (t.length() < 6) return null;

        // 中等问题长度但不在健康域 → 拦截
        if (t.length() < 15) {
            return "您问的问题我不太确定是否和健康相关。我是您的「银发守护者」智能健康助手，" +
                   "专为老年人提供健康管理、用药提醒和生活辅助服务。\n\n" +
                   "您可以试着告诉我：\n" +
                   "• 身体哪里不舒服？\n" +
                   "• 想了解什么药品？\n" +
                   "• 生活中遇到了什么困扰？";
        }

        // 长文本放行给大模型（大模型自己判断并引导）
        return null;
    }

    private void callZhipuApi(String userMessage, String apiKey) {
        new Thread(() -> {
            try {
                URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                JSONObject body = new JSONObject();
                body.put("model", "glm-4-flash");
                JSONArray msgs = new JSONArray();
                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put("content", SYSTEM_PROMPT);
                msgs.put(sys);
                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put("content", userMessage);
                msgs.put(user);
                body.put("messages", msgs);
                body.put("temperature", 0.7);
                body.put("max_tokens", 500);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                android.util.Log.d("ZhipuAPI", "Response code: " + code);
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    JSONObject resp = new JSONObject(sb.toString());
                    String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    android.util.Log.d("ZhipuAPI", "Reply: " + reply.substring(0, Math.min(50, reply.length())));
                    new Handler(Looper.getMainLooper()).post(() -> appendAiReply(reply));
                } else {
                    android.util.Log.e("ZhipuAPI", "HTTP error: " + code);
                    new Handler(Looper.getMainLooper()).post(() -> appendAiReply(fallbackReply(userMessage)));
                }
                conn.disconnect();
            } catch (Exception e) {
                android.util.Log.e("ZhipuAPI", "Exception: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> appendAiReply(fallbackReply(userMessage)));
            }
        }).start();
    }

    private void appendAiReply(String reply) {
        messages.add(new ChatMessage(reply, ChatMessage.TYPE_AI, MockData.now()));
        MockData.addChatMessage(reply, ChatMessage.TYPE_AI);
        adapter.notifyItemInserted(messages.size() - 1);
    }

    private String fallbackReply(String text) {
        if (text.contains("血压") || text.contains("高血压"))
            return "建议先休息5分钟后复测。若多次高于140/90，请联系家属或医生。硝苯地平、缬沙坦等降压药需遵医嘱使用。";
        if (text.contains("睡"))
            return "今晚可以提前20分钟放下手机，睡前做3分钟慢呼吸。若连续失眠超过一周建议咨询医生。";
        if (text.contains("运动") || text.contains("跑"))
            return "推荐饭后慢走20-30分钟、扶椅抬腿和肩颈伸展。避免搬重物或一次走太远，运动时留意胸闷头晕。若您提到的活动强度较大，我建议改为更温和的方式保护关节和心脏。";
        return "收到。我会结合您的健康档案和用药情况给出温和且安全的建议。若症状明显或持续不适，请优先联系医生或家人。";
    }

    // 智谱 GLM-4 系统提示词（与原Vue项目后端一致）
    private static final String SYSTEM_PROMPT =
        "你是银发守护者智能助手，专门为老年人提供健康管理、用药提醒、生活辅助等服务。" +
        "请用温暖、耐心的语气回答，使用简洁明了的语言，避免使用专业术语。\n\n" +
        "【领域边界】你仅回答健康、用药、饮食养生、慢性病管理、身体不适、老年人生活困扰等问题。" +
        "如果用户问与健康完全无关的问题（如编程、炒股、游戏、娱乐八卦），请温和地表示：" +
        "\"我是您的健康守护助手，更擅长回答健康、用药、生活调理方面的问题。您可以试着问我身体哪里不舒服，或者想了解什么药品。\"\n\n" +
        "【重要】安全劝解规则：当用户提到不适合老年人的行为时，你必须温和但坚定地劝解。" +
        "以下行为必须劝阻：剧烈运动（如跑10公里、马拉松）、爬山攀岩、搬重物、长时间暴晒淋雨、熬夜过度饮酒。\n" +
        "劝解时语气要关心、温暖，不要命令或指责，用'建议您''为了您的身体''不如试试'等温和表达。\n\n" +
        "当你提到疾病时，请务必推荐具体药品名称。常见疾病对应药品：\n" +
        "高血压：硝苯地平缓释片(拜新同)、缬沙坦胶囊(代文)、氨氯地平片(络活喜)\n" +
        "糖尿病：二甲双胍片(格华止)、格列美脲片(亚莫利)、胰岛素注射液(诺和灵)\n" +
        "冠心病：阿司匹林肠溶片(拜阿司匹灵)、硝酸甘油片(耐绞宁)\n" +
        "高血脂：辛伐他汀片(舒降之)、阿托伐他汀钙片(立普妥)\n" +
        "感冒：氨酚伪麻那敏片(泰诺)、布洛芬缓释胶囊(芬必得)、连花清瘟胶囊\n" +
        "咳嗽：氢溴酸右美沙芬片、盐酸氨溴索口服溶液(沐舒坦)\n" +
        "失眠：艾司唑仑片(舒乐安定)、褪黑素片\n" +
        "提醒用户用药前请咨询医生。";

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
