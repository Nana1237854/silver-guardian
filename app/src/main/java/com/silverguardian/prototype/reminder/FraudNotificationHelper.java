package com.silverguardian.prototype.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.silverguardian.prototype.FraudDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.FraudTip;

/**
 * 防诈提醒通知工具类。
 *
 * 通知设计原则（适老化）：
 * - 标题简洁："今日防诈提醒"
 * - 内容简短明了，不吓人
 * - 点击通知进入详情页
 * - 使用独立通知渠道，方便老人在系统设置中管理
 */
public class FraudNotificationHelper {
    /** 通知渠道 ID，在系统设置中可见 */
    public static final String FRAUD_CHANNEL_ID = "fraud_reminder";
    /** 通知渠道名称（系统设置中显示） */
    private static final String CHANNEL_NAME = "防诈提醒";
    /** 通知 ID 基础值 */
    private static final int NOTIFICATION_ID_BASE = 901;

    /**
     * 创建通知渠道（Android 8.0+）。
     * 在 Application.onCreate 或首次发送通知前调用。
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                FRAUD_CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("每日防诈骗知识提醒，帮助长辈识别常见骗局");
            // 不震动、不响铃，避免吓到老人
            channel.setVibrationPattern(new long[]{0});
            channel.enableVibration(false);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 发送防诈提醒通知。
     * 点击通知进入 FraudDetailActivity 查看对应详情。
     *
     * @param context 上下文
     * @param tip     当天选中的防诈条目
     */
    public static void sendFraudNotification(Context context, FraudTip tip) {
        // 构造通知内容（适合老人阅读的简短文案）
        String contentText = tip.title;
        if (tip.summary != null && !tip.summary.isEmpty()) {
            contentText = tip.title + "，" + tip.summary;
        }
        // 限制通知内容长度，避免被截断后不可读
        if (contentText.length() > 80) {
            contentText = contentText.substring(0, 77) + "…";
        }

        // 点击通知 → 打开详情页
        Intent intent = new Intent(context, FraudDetailActivity.class);
        intent.putExtra("fraud_id", tip.id);
        intent.putExtra("fraud_title", tip.title);
        intent.putExtra("fraud_category", tip.category);
        intent.putExtra("fraud_summary", tip.summary);
        intent.putExtra("fraud_detail", tip.detail);
        intent.putExtra("fraud_risk", tip.risk);
        intent.putExtra("fraud_advice", tip.advice);
        intent.putExtra("fraud_source_name", tip.sourceName);
        intent.putExtra("fraud_source_type", tip.sourceType);
        intent.putExtra("fraud_source_date", tip.sourceDate);
        intent.putExtra("fraud_is_remote", !"local".equals(tip.sourceType));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pending = PendingIntent.getActivity(context,
            NOTIFICATION_ID_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FRAUD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(context.getString(R.string.fraud_notification_title))
            .setContentText(contentText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) {
            try {
                nm.notify(NOTIFICATION_ID_BASE, builder.build());
            } catch (SecurityException e) {
                // 通知权限未授予，静默跳过
            }
        }
    }
}
