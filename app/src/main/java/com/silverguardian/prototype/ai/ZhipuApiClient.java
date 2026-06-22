package com.silverguardian.prototype.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 智谱GLM-4 Flash API客户端：网络请求、JSON解析、主线程回调
 */
public class ZhipuApiClient {
    private static final String TAG = "ZhipuApiClient";
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    public interface Callback {
        void onSuccess(String reply);
        void onFailure(String error);
    }

    // 调用智谱GLM-4 Flash API：构建JSON请求→HTTP POST→回调主线程
    public void chat(String apiKey, String userMessage, String systemPrompt, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
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
                sys.put("content", systemPrompt);
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
                Log.d(TAG, "Response code: " + code);
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    JSONObject resp = new JSONObject(sb.toString());
                    String reply = resp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    Log.d(TAG, "Reply: " + reply.substring(0, Math.min(50, reply.length())));
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(reply));
                } else {
                    Log.e(TAG, "HTTP error: " + code);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("HTTP " + code));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }
}
