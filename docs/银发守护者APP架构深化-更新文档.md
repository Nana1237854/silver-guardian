# 银发守护者 APP 架构深化 — 更新文档

> 更新日期：2026-06-21  
> 项目：银发守护者 APP（Silver Guardian）  
> 更新类型：渐进式架构深化  
> 对应方案：`docs/银发守护者APP架构深化方案_修正版.md`  
> 对应 ADR：`docs/adr/0004-receiver-data-seam.md`、`docs/adr/0005-unified-health-evaluation.md`、`docs/adr/0006-notification-tts-seam.md`

---

## 一、更新概述

本次更新不是新增业务功能，而是在不破坏现有页面和数据库结构的前提下，对现有代码进行渐进式架构深化。

本轮严格按照既定阶段顺序推进，实际落地内容如下：

1. 完成 **ADR-0004：修复 ReminderBroadcastReceiver 数据接缝**
2. 完成 **ADR-0005：统一健康评估逻辑**
3. 完成 **ADR-0006：抽离通知接缝与 TTS 接缝**
4. 对 **ADR-0003：Repository 渐进式拆分** 仅输出迁移计划，暂不做大规模 Store 拆分

本次更新遵循以下约束：

- 不引入 Room
- 不修改数据库表结构
- 不删除 Repository
- 不删除现有 Module
- 不删除 TtsHelper
- 不一次性重写 Reminder、Health、AI Chat 相关链路

---

## 二、本次实际完成的架构深化内容

### 2.1 第一阶段：修复 ReminderBroadcastReceiver 数据接缝

目标是让 `ReminderBroadcastReceiver` 不再直接访问 SQLite，不再硬编码数据库名，也不再手写 SQL。

本次调整后：

- 在 `Repository` 中新增 `isMedicineTakenToday(int userId, int medicineId)`
- `ReminderBroadcastReceiver` 改为通过 `Repository.init(context)` 懒初始化仓储
- 是否已服药的判断统一改为调用 `Repository.isMedicineTakenToday(...)`
- Receiver 中原有的通知、TTS 播报、重复提醒调度逻辑保持不变
- `MedicineAlarmScheduler.scheduleAt()` 的原有调用链保持不变

调整效果：

- Receiver 不再出现 `openOrCreateDatabase(...)`
- Receiver 不再出现 `rawQuery(...)`
- `medicine_taken` 的查询逻辑集中到 DAO / Repository 路径中

### 2.2 第二阶段：统一健康评估逻辑

目标是让健康指标的状态展示与异常级别判断来自同一份规则，避免 UI 和告警逻辑分叉。

本次调整后：

- 在 `health` 包中新增 `AlertLevel`
- 在 `health` 包中新增 `EvaluationResult`
- `HealthMetricEvaluator` 新增 `evaluate(String type, String value)`
- `HealthMetricEvaluator.status(...)` 仍然保留，但内部已改为复用 `evaluate(...)`
- `HealthAlertService` 不再维护自己的阈值 `switch(type)` 判断
- `Repository.addHealthData()` 在未传入状态时，统一使用 `HealthMetricEvaluator.evaluate(...).displayLabel`

当前统一评估的指标包括：

- `heart_rate`
- `blood_pressure`
- `blood_oxygen`
- `temperature`
- `blood_sugar`
- `respiratory_rate`

统一后的收益：

- 页面显示状态和告警等级来自同一份结果
- 新增或调整阈值时只需要修改一处
- 健康记录、健康告警、后续扩展测试更容易保持一致

### 2.3 第三阶段：抽离通知接缝

目标是让 `HealthAlertService` 和 `ReminderBroadcastReceiver` 不再各自手写通知渠道、`PendingIntent` 和 `NotificationCompat.Builder`。

本次新增：

- `notification/NotificationRequest.java`
- `notification/NotificationDispatcher.java`
- `notification/AndroidNotificationDispatcher.java`

本次改造后：

- 健康告警通知改为构造 `NotificationRequest` 后交给 `AndroidNotificationDispatcher`
- 用药提醒通知改为构造 `NotificationRequest` 后交给 `AndroidNotificationDispatcher`
- 通知渠道创建统一集中到 `AndroidNotificationDispatcher`
- Android 13+ 通知权限检查统一集中处理
- `SecurityException` 被捕获，避免因为通知权限或系统限制导致 App 崩溃

业务层保持不变的内容：

- 通知文案未改变
- 点击通知仍跳转到原有 `MainActivity`
- 健康告警与用药提醒的原有业务触发时机未改变

### 2.4 第四阶段：薄封装 TTS 接缝

目标是让 `HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule` 不再直接依赖 `TtsHelper.speak()`。

本次新增：

- `tts/TtsAdapter.java`
- `tts/AndroidTtsAdapter.java`

本次改造后：

- `HealthAlertService` 改为依赖 `TtsAdapter`
- `ReminderBroadcastReceiver` 改为依赖 `TtsAdapter`
- `AiChatModule` 改为依赖 `TtsAdapter`
- `AndroidTtsAdapter` 底层仍然调用现有 `TtsHelper`
- `ReminderBroadcastReceiver` 仍保留 `TtsHelper.init(context)`，避免一次性改动 TTS 生命周期

本次处理属于“薄封装”而不是“TTS 子系统重写”，因此：

- `TtsHelper` 仍然保留
- TTS 生命周期没有被大规模重构
- 现有语音播报能力可继续复用

### 2.5 第五阶段：Repository 拆分仅做设计准备

本次没有直接拆掉 `Repository`，也没有大规模修改 `Module`、`Fragment` 或 `Activity`。

仅新增：

- `docs/architecture/store-migration-plan.md`

该迁移计划覆盖：

- 当前 Repository 职责拆分表
- 各 Store 的 DAO 归属建议
- 各 Store 的建议接口
- 可保留的 Module 兼容门面
- 后续适合迁移到 DomainEventBus 的副作用
- 推荐 PR 拆分顺序和每个 PR 的验收标准

---

## 三、关键改动文件

### 3.1 数据接缝与健康评估

- `app/src/main/java/com/silverguardian/prototype/data/Repository.java`
- `app/src/main/java/com/silverguardian/prototype/reminder/ReminderBroadcastReceiver.java`
- `app/src/main/java/com/silverguardian/prototype/health/HealthMetricEvaluator.java`
- `app/src/main/java/com/silverguardian/prototype/health/HealthAlertService.java`
- `app/src/main/java/com/silverguardian/prototype/health/AlertLevel.java`
- `app/src/main/java/com/silverguardian/prototype/health/EvaluationResult.java`

### 3.2 通知与 TTS 接缝

- `app/src/main/java/com/silverguardian/prototype/notification/NotificationRequest.java`
- `app/src/main/java/com/silverguardian/prototype/notification/NotificationDispatcher.java`
- `app/src/main/java/com/silverguardian/prototype/notification/AndroidNotificationDispatcher.java`
- `app/src/main/java/com/silverguardian/prototype/tts/TtsAdapter.java`
- `app/src/main/java/com/silverguardian/prototype/tts/AndroidTtsAdapter.java`
- `app/src/main/java/com/silverguardian/prototype/ai/AiChatModule.java`
- `app/src/main/java/com/silverguardian/prototype/MainActivity.java`

### 3.3 计划文档

- `docs/architecture/store-migration-plan.md`

---

## 四、当前架构结果

经过本轮调整后，相关链路已经形成以下更清晰的分层关系：

```text
ReminderBroadcastReceiver
  -> Repository
  -> MedicineDao.isTakenToday()
  -> NotificationDispatcher
  -> TtsAdapter

HealthAlertService
  -> HealthMetricEvaluator.evaluate()
  -> NotificationDispatcher
  -> TtsAdapter

AiChatModule
  -> TtsAdapter

Repository.addHealthData()
  -> HealthMetricEvaluator.evaluate()
```

这意味着：

- Receiver 不再绕开 Repository 直接查库
- 健康阈值判断不再在多个类中重复维护
- 通知发送入口被集中
- TTS 依赖被薄封装，后续更容易替换或注入测试实现

---

## 五、验证结果

本轮架构深化完成后，已进行以下验证：

- `ReminderBroadcastReceiver` 中不再出现 `openOrCreateDatabase`
- `ReminderBroadcastReceiver` 中不再出现 `rawQuery` 查询 `medicine_taken`
- `HealthAlertService` 不再包含重复的健康阈值 `switch`
- `HealthAlertService` 与 `ReminderBroadcastReceiver` 不再直接构建 `NotificationCompat.Builder`
- `HealthAlertService`、`ReminderBroadcastReceiver`、`AiChatModule` 不再直接调用 `TtsHelper.speak()`
- 新增并补强了 `HealthMetricEvaluatorTest`

本地构建 / 测试结果：

- `:app:testDebugUnitTest` 通过
- `:app:compileDebugJavaWithJavac` 通过

---

## 六、这次没有做的事情

为了保证“渐进式重构”而不是“一次性重写”，本次刻意没有做以下内容：

- 没有引入 Room
- 没有修改数据库结构
- 没有删除 `Repository`
- 没有删除任何 `Module`
- 没有把 `Repository` 直接拆成多个 Store 并全量替换调用方
- 没有引入 `DomainEventBus` 正式实现
- 没有重写 TTS 生命周期
- 没有重写 AI 对话模块的全部职责拆分

这部分内容已在 ADR 和迁移计划中预留后续演进方向。

---

## 七、后续建议

建议后续继续按小步提交推进，而不是再次做跨模块大改：

1. 先按 `store-migration-plan.md` 为 `HealthRecordStore` 与 `MedicinePlanStore` 做最小可用抽取
2. 保留现有 `Module` 作为兼容门面，先不改页面层
3. 等 Store 接口稳定后，再逐步收敛 `BaseFragment` / `BaseActivity` 的访问方式
4. 最后再评估是否引入 `DomainEventBus` 迁移通知、TTS、告警等副作用

---

## 八、课程答辩可用表述

本次架构深化最适合从“在不破坏现有功能的前提下，降低耦合、统一规则、为后续演进留接缝”这个角度进行介绍。

可以重点说明四点：

1. **数据接缝收敛**：用药提醒广播不再直接查 SQLite，而是统一走 Repository
2. **规则单一来源**：健康状态展示和健康告警都复用同一个评估器
3. **平台能力适配集中**：通知渠道、通知构建、权限处理统一收口
4. **副作用薄封装**：TTS 通过适配器隔离，降低业务模块对具体实现的直接依赖

这样既能体现架构思路，也能证明本项目采用的是可落地、可编译、可渐进演进的重构方式。
