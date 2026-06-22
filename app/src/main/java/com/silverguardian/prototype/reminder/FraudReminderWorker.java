package com.silverguardian.prototype.reminder;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.silverguardian.prototype.FraudApiClient;
import com.silverguardian.prototype.SilverGuardianApp;
import com.silverguardian.prototype.models.FraudTip;
import com.silverguardian.prototype.modules.SafetyContentModule;

import java.util.List;

/**
 * WorkManager 周期任务：每日防诈提醒 Worker。
 *
 * 执行流程：
 * 1. 通过 OkHttp 拉取远程防诈 JSON（FraudApiClient.fetchFraudTips 同步版）
 * 2. 拉取成功则使用远程内容；失败则使用本地预置内容
 * 3. 从内容列表中选取一条今日提醒
 * 4. 通过 NotificationManager 发送系统通知
 * 5. 用户点击通知 → FraudDetailActivity 查看详情
 *
 * 适用场景：用户开启"每日防诈提醒"后，WorkManager 每天早上执行一次。
 * 不要求秒级精确，WorkManager 会自动选择合适时机执行。
 */
public class FraudReminderWorker extends Worker {
    private static final String TAG = "FraudReminderWorker";

    public FraudReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            // 同步拉取远程防诈内容
            List<FraudApiClient.FraudItem> remoteItems = FraudReminderScheduler.fetchRemoteSync();
            FraudTip selected;

            if (remoteItems != null && !remoteItems.isEmpty()) {
                // 取第一条作为今日提醒
                FraudApiClient.FraudItem item = remoteItems.get(0);
                selected = new FraudTip(
                    item.id, item.title, item.category, item.summary,
                    item.detail, item.risk, item.advice,
                    item.sourceName, item.sourceType, item.sourceDate);
            } else {
                // 网络失败，使用本地预置内容
                SafetyContentModule safety = SilverGuardianApp.from(context).safetyContent();
                List<FraudTip> localTips = safety.getFraudTips();
                if (localTips.isEmpty()) {
                    Log.w(TAG, "No fraud tips available, skipping reminder");
                    return Result.success();
                }
                // 根据当天日期选择一条（避免每天重复同一条）
                int index = (int) (System.currentTimeMillis() / (1000 * 60 * 60 * 24)) % localTips.size();
                selected = localTips.get(index);
            }

            FraudNotificationHelper.sendFraudNotification(context, selected);
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "FraudReminderWorker failed", e);
            // 失败也返回 success，避免 WorkManager 无限重试
            return Result.success();
        }
    }
}
