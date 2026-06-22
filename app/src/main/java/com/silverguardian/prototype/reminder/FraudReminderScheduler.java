package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.silverguardian.prototype.FraudApiClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 每日防诈提醒任务调度：WorkManager周期性任务注册
/**
 * 每日防诈提醒调度器。
 * 封装 WorkManager 周期任务的注册和取消。
 *
 * 常量集中管理：
 * - FRAUD_REMINDER_WORK_NAME: WorkManager 任务唯一标识
 */
public class FraudReminderScheduler {
    /** WorkManager 周期任务唯一名称，用于注册/取消 */
    public static final String FRAUD_REMINDER_WORK_NAME = "daily_fraud_reminder";

    private static final String TAG = "FraudReminderScheduler";
    private static final OkHttpClient syncClient = new OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build();

    /**
     * 注册每日防诈提醒周期任务。
     * 使用 PeriodicWorkRequest，最小周期 15 分钟，实际每天约执行一次。
     * WorkManager 会自动选择设备空闲、有网时执行。
     *
     * @param context 上下文
     */
    public static void scheduleDailyReminder(Context context) {
        // 不要求网络（Worker 内部有离线兜底），确保每天都能推送提醒
        Constraints constraints = new Constraints.Builder()
            .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            FraudReminderWorker.class, 24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(FRAUD_REMINDER_WORK_NAME)
            .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FRAUD_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest);

        Log.d(TAG, "Daily fraud reminder scheduled");
    }

    /**
     * 取消每日防诈提醒周期任务。
     *
     * @param context 上下文
     */
    public static void cancelDailyReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(FRAUD_REMINDER_WORK_NAME);
        Log.d(TAG, "Daily fraud reminder cancelled");
    }

    /**
     * 查询当前是否已注册每日提醒。
     * 简化实现：通过 SharedPreferences 记录用户开关状态。
     */
    public static boolean isReminderEnabled(Context context) {
        return context.getSharedPreferences("elder_session", Context.MODE_PRIVATE)
            .getBoolean("fraud_daily_reminder", false);
    }

    /**
     * 记录用户开关状态。
     */
    public static void setReminderEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("elder_session", Context.MODE_PRIVATE)
            .edit().putBoolean("fraud_daily_reminder", enabled).apply();
    }

    /**
     * 同步 OkHttp 拉取远程 JSON（供 Worker 在后台线程使用）。
     * 返回解析后的 FraudItem 列表，失败返回 null。
     */
    static List<FraudApiClient.FraudItem> fetchRemoteSync() {
        // TODO: 替换为正式 JSON 接口地址
        String url = "https://raw.githubusercontent.com/Nana1237854/silver-guardian/master/app/src/main/assets/fraud_api_data.json";
        Request request = new Request.Builder()
            .url(url)
            .header("User-Agent", "SilverGuardian/1.0")
            .build();
        try (Response response = syncClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String body = response.body().string();
            return FraudApiClient.parseFraudResponse(body);
        } catch (Exception e) {
            Log.e(TAG, "Sync fetch failed: " + e.getMessage());
            return null;
        }
    }
}
