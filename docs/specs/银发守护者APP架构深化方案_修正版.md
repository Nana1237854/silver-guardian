# 银发守护者 APP 架构深化方案：修正版

> 适用于 Android Studio 项目 `silver-guardian` 的渐进式重构方案。  
> 核心原则：不一次性大重构，先修复清晰边界，再逐步统一规则、抽离接缝，最后推进 Repository 按领域拆分。

---

## 总体落地顺序

本次架构深化不采用一次性大重构，而采用渐进式重构：

1. 先修复边界最清晰的问题：`ReminderBroadcastReceiver` 不再直连 SQLite。
2. 再统一健康指标评估逻辑，避免 UI 状态和告警阈值重复判断。
3. 再抽离通知与 TTS 接缝，减少 `HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule` 的重复代码。
4. 最后再做 `Repository` 按领域拆分，并且第一阶段保留现有 `Module` 作为兼容门面。

推荐落地顺序：

```text
ADR-0004 Receiver 数据接缝
ADR-0005 统一健康评估逻辑
ADR-0006 通知与 TTS 接缝
ADR-0003 Repository / Store 渐进式拆分
```

---

# ADR-0003 Repository 渐进式拆分 + Module 兼容门面 + AiChatModule 拆分

## Status

Proposed

## Context

当前 `Repository.java` 同时承担多个领域的数据访问、内存缓存和副作用编排职责，包括用户、健康档案、用药提醒、相册、聊天、记忆、紧急告警、防诈骗内容等。

当前问题包括：

1. 职责过重，接近“上帝模块”。
2. `Module`、`Activity`、`Fragment`、`Service` 都依赖同一个 `Repository` 接缝。
3. 某些 `Module` 只是简单透传 `Repository` 方法。
4. `Repository` 中还直接调用 `HealthAlertService`、`MedicineAlarmScheduler` 等 Android 副作用模块。
5. 后续测试和功能扩展会越来越困难。
6. `AiChatModule` 同时负责语音识别、API 通信、内容过滤、提示词构建、药品推荐解析和 TTS 调用，职责偏重。

## Decision 1：Repository 最终按领域拆分为 Store

目标状态下，每个领域对应一个 `Store`：

| Store | 职责 |
|---|---|
| `UserStore` | 老人、家属、多用户切换、当前用户信息 |
| `HealthRecordStore` | 健康档案、蓝牙设备、健康数据写入 |
| `MedicinePlanStore` | 用药提醒、服药打卡、药品库 |
| `AlbumStore` | 家人相册 |
| `ChatStore` | 智能对话消息 |
| `MemoryStore` | 记忆记录 |
| `EmergencyStore` | 紧急告警 |
| `FraudTipStore` | 防诈骗内容 |

`AppContainer` 负责构建和注入这些 `Store`。

## Decision 2：不立即删除 Module，而是保留为兼容门面

第一阶段不直接删除 `HealthRecordModule`、`MedicineReminderModule`、`FamilyAlbumModule`、`MemoryModule`、`SafetyContentModule`、`UserSessionModule` 等现有 `Module`。

原因：

1. 当前页面层大量通过 `BaseFragment` / `BaseActivity` 获取 `Module`。
2. 直接删除 `Module` 会导致页面层大面积修改。
3. 一次性重构风险过高，不利于课程项目稳定运行。

第一阶段采用：

```text
Fragment / Activity
  ↓
Module
  ↓
Store
  ↓
DAO
```

`Module` 从 `Repository` 适配器变成 `Store` 适配器。

等 `Store` 稳定后，再逐步迁移为：

```text
Fragment / Activity
  ↓
Store
  ↓
DAO
```

最后再删除没有使用者的纯透传 `Module`。

## Decision 3：SessionStore 只管理 activeUserId

新增 `SessionStore`，仅负责：

1. 保存 `activeUserId`。
2. 读取当前用户 ID。
3. 切换当前用户。
4. 通知其他 `Store` 重新加载当前用户数据。

不采用销毁并重建所有 `Store` 的模式，避免引用悬空和内存抖动。

## Decision 4：副作用逐步从 Store 中抽离

长期目标是引入轻量 `DomainEventBus`：

```text
HealthRecordStore.addHealthData()
  ↓
发布 HealthDataAdded

MedicinePlanStore.addMedicine()
  ↓
发布 MedicineAdded

MedicinePlanStore.deleteMedicine()
  ↓
发布 MedicineDeleted
```

订阅方：

```text
HealthAlertService 订阅 HealthDataAdded
MedicineAlarmScheduler 订阅 MedicineAdded / MedicineDeleted
```

但 `DomainEventBus` 不在第一阶段强制落地，避免 `ADR-0003` 的改动过大。

第一阶段可以先做到：

1. `Store` 内不直接拼复杂 UI 文案。
2. `Store` 对 Android 副作用的调用逐步减少。
3. 先把数据职责拆开，再处理事件解耦。

## Decision 5：AiChatModule 拆分为薄编排器

`AiChatModule` 最终变成编排器，内部注入以下组件：

| 组件 | 职责 |
|---|---|
| `VoiceInputAdapter` | 封装 `SpeechRecognizer` 的 `start / stop / destroy` |
| `ZhipuApiClient` | 负责智谱 API 请求 |
| `ContentFilter` | 内容过滤 |
| `DrugRecommendationParser` | 药品推荐解析 |
| `PromptBuilder` | 构建系统提示词 |
| `TtsAdapter` | 播放 AI 回复语音 |

权限申请仍由 `Activity` / `AiChatModule` 协调，不放入 `VoiceInputAdapter`，因为 Android 权限回调必须落在 `Activity` / `Fragment` 生命周期中。

## Consequences

优点：

1. `Repository` 职责逐步变小。
2. 页面层不需要一次性大改。
3. `Store` 可以逐步独立测试。
4. `Module` 作为兼容层，可以降低重构风险。
5. `AiChatModule` 的语音、API、提示词、TTS 职责更清晰。

代价：

1. 第一阶段会同时存在 `Repository`、`Store`、`Module`，结构会短期变复杂。
2. 需要分多个 PR 渐进迁移。
3. 需要明确每个阶段的验收标准，避免半重构状态长期存在。

---

# ADR-0004 ReminderBroadcastReceiver 统一使用数据接缝

## Status

Proposed

## Context

`ReminderBroadcastReceiver.onReceive()` 当前直接调用 `openOrCreateDatabase("elderly_guardian.db", ...)` 并手写 SQL 查询 `medicine_taken`。

这会导致：

1. 数据库名称重复硬编码。
2. `Receiver` 绕过 `MedicineDao`。
3. 是否已服药的判断逻辑出现多个数据源。
4. 测试 `Receiver` 时需要真实 SQLite 文件。
5. 后续数据库结构调整时容易漏改 `Receiver`。

项目中 `MedicineDao` 已经提供 `isTakenToday(uid, mid, day)`，因此 `Receiver` 不应该再直接查 SQLite。

## Decision

新增统一查询方法：

```java
public boolean isMedicineTakenToday(int userId, int medicineId)
```

第一阶段放在 `Repository` 中实现：

```java
public boolean isMedicineTakenToday(int userId, int medicineId) {
    return medicineDao.isTakenToday(userId, medicineId, today());
}
```

`ReminderBroadcastReceiver.onReceive()` 改为：

```text
onReceive()
  ↓
Repository repo = Repository.init(context)
  ↓
repo.isMedicineTakenToday(userId, medicineId)
  ↓
如果已服药，则不再提醒
  ↓
否则发送通知和 TTS
```

未来 `ADR-0003` 落地后，该方法迁移到 `MedicinePlanStore`：

```java
medicinePlanStore.isTakenToday(userId, medicineId)
```

## Consequences

优点：

1. 删除 `Receiver` 中的 `openOrCreateDatabase`。
2. 删除 `Receiver` 中的手写 SQL。
3. `medicine_taken` 查询逻辑统一回到 `MedicineDao`。
4. `Receiver` 不关心数据库名称和表结构。
5. 更容易测试。

注意：

1. `Repository.init(context)` 在 `Receiver` 中需要允许懒初始化。
2. `Repository.init(context)` 不应触发危险副作用，只做 `DbHelper` 创建和数据加载。
3. 这一步不引入 `Store`，避免和 `ADR-0003` 绑定过深。

---

# ADR-0005 统一健康评估逻辑

## Status

Proposed

## Context

当前 `HealthMetricEvaluator.status()` 和 `HealthAlertService.evaluate()` 都在解析健康指标类型并判断阈值。

问题：

1. 同一套健康指标阈值分散在两个地方。
2. UI 状态和告警等级可能不一致。
3. 新增指标类型时需要改两处。
4. `HealthAlertService` 同时负责告警发送和阈值判断，职责偏重。

## Decision

新增两个独立类型：

```java
public enum AlertLevel {
    NORMAL,
    WARNING,
    CRITICAL
}
```

```java
public final class EvaluationResult {
    public final String displayLabel;
    public final AlertLevel alertLevel;

    public EvaluationResult(String displayLabel, AlertLevel alertLevel) {
        this.displayLabel = displayLabel;
        this.alertLevel = alertLevel;
    }
}
```

`HealthMetricEvaluator` 成为唯一阈值判断入口：

```java
public static EvaluationResult evaluate(String type, String value)
```

保留旧方法用于兼容：

```java
public static String status(String type, String value) {
    return evaluate(type, value).displayLabel;
}
```

`HealthAlertService` 不再包含 `switch(type)` 阈值判断，而是：

```java
EvaluationResult result = HealthMetricEvaluator.evaluate(data.type, data.value);

if (result.alertLevel == AlertLevel.NORMAL) {
    return;
}

// 触发通知、TTS 和紧急告警记录
```

`Repository.addHealthData()` 写入健康数据时，也使用同一个 `EvaluationResult`：

```text
用户录入健康数据
  ↓
HealthMetricEvaluator.evaluate(type, value)
  ↓
displayLabel 保存到 HealthData.status
  ↓
alertLevel 交给 HealthAlertService 判断是否告警
```

## Consequences

优点：

1. 阈值逻辑只有一个来源。
2. UI 展示和告警判断保持一致。
3. 新增健康指标只需要改 `HealthMetricEvaluator`。
4. `HealthAlertService` 更专注于告警行为。
5. 后续接入 `DomainEventBus` 时，`HealthDataAdded` 可以携带 `EvaluationResult`。

注意：

1. `AlertLevel` 不要作为 `HealthAlertService` 的内部 enum。
2. `HealthMetricEvaluator` 不要反向依赖 `HealthAlertService`。
3. 本阶段不强制引入 `DomainEventBus`，避免改动过大。

---

# ADR-0006 通知与 TTS 统一接缝

## Status

Proposed

## Context

当前 `HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule` 都直接调用 `TtsHelper.speak()`。

同时，`HealthAlertService` 和 `ReminderBroadcastReceiver` 分别创建通知渠道、构建 `PendingIntent`、构建 `NotificationCompat.Builder`，导致通知逻辑重复。

问题：

1. 通知渠道创建分散。
2. `PendingIntent` 构建分散。
3. Android 13+ 通知权限处理不集中。
4. TTS 调用分散。
5. 测试通知和 TTS 行为困难。
6. 后续修改通知样式需要改多个文件。

## Decision 1：新增 NotificationRequest

不使用过长参数的 `notify` 方法，而是新增请求对象：

```java
public final class NotificationRequest {
    public final int targetUserId;
    public final String channelId;
    public final String channelName;
    public final String title;
    public final String message;
    public final int smallIconRes;
    public final Intent targetIntent;
    public final int importance;
    public final boolean autoCancel;
}
```

必要时可以增加：

```java
public final int notificationId;
```

`notificationId` 可以由 `Dispatcher` 统一生成，也可以由调用方传入。

## Decision 2：新增 NotificationDispatcher

```java
public interface NotificationDispatcher {
    void notify(Context context, NotificationRequest request);
}
```

默认实现：

```java
public final class AndroidNotificationDispatcher implements NotificationDispatcher
```

负责：

1. Android O+ 创建 `NotificationChannel`。
2. 构建 `PendingIntent`。
3. 构建 `NotificationCompat.Builder`。
4. 检查通知权限。
5. 捕获 `SecurityException`。
6. 发送通知。

## Decision 3：HealthAlertService 和 ReminderBroadcastReceiver 使用 NotificationDispatcher

`HealthAlertService` 不再自己创建通知渠道和 `NotificationCompat.Builder`。

`ReminderBroadcastReceiver` 也不再自己创建通知渠道和 `NotificationCompat.Builder`。

它们只构造 `NotificationRequest`，然后交给 `NotificationDispatcher`。

## Decision 4：新增 TtsAdapter

```java
public interface TtsAdapter {
    void speak(String text);
    void stop();
}
```

第一阶段实现可以很薄：

```java
public final class AndroidTtsAdapter implements TtsAdapter {
    @Override
    public void speak(String text) {
        TtsHelper.speak(text);
    }

    @Override
    public void stop() {
        // 如果 TtsHelper 暂无 stop 方法，先留空
    }
}
```

如果当前 `TtsHelper` 没有 `stop()`，可以先只实现 `speak()`，或者 `stop()` 留空实现。

调用方：

1. `HealthAlertService`。
2. `ReminderBroadcastReceiver`。
3. `AiChatModule`。

## Decision 5：分阶段落地

第一阶段：

1. 新增 `NotificationRequest`。
2. 新增 `NotificationDispatcher`。
3. 新增 `AndroidNotificationDispatcher`。
4. `HealthAlertService` 使用 `NotificationDispatcher`。
5. `ReminderBroadcastReceiver` 使用 `NotificationDispatcher`。

第二阶段：

1. 新增 `TtsAdapter`。
2. `AiChatModule`、`HealthAlertService`、`ReminderBroadcastReceiver` 改为依赖 `TtsAdapter`。
3. `TtsHelper` 变成底层实现细节。

## Consequences

优点：

1. 通知发送逻辑集中。
2. 通知权限变化只需要改 `AndroidNotificationDispatcher`。
3. `HealthAlertService` 和 `Receiver` 更专注业务。
4. TTS 引擎替换更容易。
5. 单元测试可以注入 `FakeNotificationDispatcher` 和 `FakeTtsAdapter`。

注意：

1. 不要把 `NotificationDispatcher` 设计成参数很长的工具方法。
2. `AiChatModule` 只需要 `TtsAdapter`，不应该依赖 `NotificationDispatcher`。
3. 通知与 TTS 是两个独立接缝，不要强行合并。

---

# 最终执行建议

1. 先做 `ADR-0004`，因为改动最小、收益最大。
2. 再做 `ADR-0005`，统一健康评估规则，降低重复逻辑。
3. 再做 `ADR-0006`，抽离通知与 TTS 接缝。
4. 最后做 `ADR-0003`，并坚持“保留 Module 兼容门面”的渐进式重构路线。

这套方案的核心不是推翻现有项目，而是在不破坏现有功能的前提下逐步降低耦合，提高可测试性和可维护性。
