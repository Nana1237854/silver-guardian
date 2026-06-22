package com.silverguardian.prototype;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 防诈数据API客户端：OkHttp请求远程JSON、解析防诈内容
/**
 * OkHttp 客户端 — 防诈骗知识推送内容拉取。
 *
 * 数据来源说明：
 * App 拉取的是人工整理后的统一 JSON 数据，而非直接爬取第三方网页。
 * JSON 内容整理自以下权威渠道：
 * - 公安部 / 国家反诈中心发布的防范电信网络诈骗宣传内容
 * - 12321 网络不良与垃圾信息举报受理中心
 * - 中国互联网联合辟谣平台
 * - 国家金融监督管理总局（养老诈骗、金融风险提示）
 * - 96110、12381、国家反诈中心 App 等官方反诈工具科普
 *
 * 如需替换为自建 JSON 接口，修改 REMOTE_API 常量即可。
 *
 * 技术考察点：第三方网络请求库（OkHttp）、JSON 解析容错
 */
public class FraudApiClient {
    private static final String TAG = "FraudApiClient";

    /**
     * 远程 JSON 数据地址。
     * 当前使用 GitHub Raw 作为演示数据源，正式发布时替换为自建接口。
     * 示例：https://your-server.com/api/v1/fraud/tips
     */
    // TODO: 替换为正式 JSON 接口地址
    private static final String REMOTE_API =
        "https://raw.githubusercontent.com/Nana1237854/silver-guardian/master/app/src/main/assets/fraud_api_data.json";

    private final OkHttpClient client;

    public FraudApiClient() {
        client = new OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    }

    public interface FraudCallback {
        void onSuccess(List<FraudItem> items);
        void onFailure(String error);
    }

    // 发起OkHttp异步GET请求获取远程防诈JSON数据
    public void fetchFraudTips(FraudCallback callback) {
        Request request = new Request.Builder()
            .url(REMOTE_API)
            .header("User-Agent", "SilverGuardian/1.0")
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp request failed: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    List<FraudItem> items = parseFraudResponse(body);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(items));
                } catch (Exception e) {
                    Log.e(TAG, "JSON parse error: " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() ->
                        callback.onFailure("解析失败: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * 远程 JSON 解析后的条目，包含新版完整字段。
     * 所有字段均使用 optXxx 安全读取，缺少字段时使用兜底值，不会崩溃。
     */
    public static class FraudItem {
        public String id;
        public String title;
        public String category;
        public String summary;
        public String detail;
        public String risk;
        public String advice;
        public String sourceName;
        public String sourceType;
        public String sourceDate;
        // 兼容旧版 JSON 的 measures 字段
        public String measures;
    }

    /**
     * 解析 JSON 响应。
     * 同时兼容旧版 JSON（仅含 title/category/summary/detail/measures）
     * 和新版 JSON（含 id/risk/advice/sourceName/sourceType/sourceDate）。
     * 容错策略：所有字段均使用 optXxx 并提供合理默认值，解析失败不崩溃。
     */
    public static List<FraudItem> parseFraudResponse(String json) throws Exception {
        List<FraudItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray data = root.optJSONArray("data");
        if (data == null) {
            return items; // data 字段缺失时返回空列表，由调用方使用本地内容兜底
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.optJSONObject(i);
            if (obj == null) {
                continue; // 跳过非法条目
            }
            FraudItem item = new FraudItem();
            // 新版字段
            item.id = obj.optString("id", "fraud_" + (i + 1));
            item.risk = obj.optString("risk", "");
            item.advice = obj.optString("advice", "");
            item.sourceName = obj.optString("sourceName", "权威来源整理");
            item.sourceType = obj.optString("sourceType", "official");
            item.sourceDate = obj.optString("sourceDate", "");
            // 基础字段（新版和旧版共用）
            item.title = obj.optString("title", "防诈骗提示");
            item.category = obj.optString("category", "电信诈骗");
            item.summary = obj.optString("summary", "");
            item.detail = obj.optString("detail", "");
            // 兼容旧版 measures 字段：如果新版 advice 为空，用旧版 measures 填充
            item.measures = obj.optString("measures", "");
            if (item.advice.isEmpty() && !item.measures.isEmpty()) {
                item.advice = item.measures;
            }
            // 如果 detail 为空但有 summary，用 summary 作为 detail 的兜底
            if (item.detail.isEmpty() && !item.summary.isEmpty()) {
                item.detail = item.summary;
            }
            items.add(item);
        }
        return items;
    }
}
