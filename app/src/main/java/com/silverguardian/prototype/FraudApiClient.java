package com.silverguardian.prototype;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

/**
 * OkHttp 客户端 — 防诈骗知识推送内容拉取。
 * 技术考察点：第三方网络请求库（OkHttp）
 */
public class FraudApiClient {
    private static final String TAG = "FraudApiClient";
    private static final String REMOTE_API = "https://api.liangmlk.cn/api/fraud/list?page=1&size=10";

    private final OkHttpClient client;

    public FraudApiClient() {
        client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    }

    public interface FraudCallback {
        void onSuccess(List<FraudItem> items);
        void onFailure(String error);
    }

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
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("解析失败: " + e.getMessage()));
                }
            }
        });
    }

    public static class FraudItem {
        public String title;
        public String category;
        public String summary;
        public String detail;
        public String measures;
    }

    // 提取为 package-visible 以便测试
    static List<FraudItem> parseFraudResponse(String json) throws Exception {
        List<FraudItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray data = root.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                FraudItem item = new FraudItem();
                item.title = obj.optString("title", "防诈骗提示");
                item.category = obj.optString("category", "电信诈骗");
                item.summary = obj.optString("summary", "");
                item.detail = obj.optString("detail", "");
                item.measures = obj.optString("measures", "");
                items.add(item);
            }
        }
        return items;
    }
}
