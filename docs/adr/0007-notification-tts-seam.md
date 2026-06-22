# 通知与 TTS 统一接缝

## Status

Proposed

## Context

`TtsHelper.speak()` 和 Android 通知发送被三个模块直接调用：`HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule`。每个调用方各自管理通知渠道创建、`PendingIntent` 构建、TTS 初始化。改通知行为需要改三个文件。

## Decision

分离为两个独立接缝：

### NotificationDispatcher

```java
interface NotificationDispatcher {
    void notify(Context context, int targetUserId, String channelId,
                String title, String message, Intent targetIntent);
}
```

适配器内部处理：渠道创建（Android O+）、`NotificationCompat` 构建、`PendingIntent` 构建、notification ID 生成。

调用方：`HealthAlertService`、`ReminderBroadcastReceiver`。

### TtsAdapter

接口待细化（TtsHelper 当前为静态方法）。调用方：`HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule`。

两个接缝独立——`AiChatModule` 只依赖 `TtsAdapter`，不需要知道通知渠道的存在。

## Consequences

- `HealthAlertService` 和 `ReminderBroadcastReceiver` 不再各自创建 NotificationChannel、PendingIntent、NotificationCompat.Builder
- 如果通知权限模型变化（如 Android 14+），只需改 `NotificationDispatcher` 适配器
- TTS 引擎替换只需改 `TtsAdapter` 适配器
- 单元测试时调用方可接收记录通知/TTS 调用的假适配器
