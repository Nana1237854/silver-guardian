# ReminderBroadcastReceiver 统一使用数据接缝

## Status

Proposed

## Context

`ReminderBroadcastReceiver.onReceive()` 直接调用 `context.openOrCreateDatabase("elderly_guardian.db", ...)` 硬编码数据库名称，并手写 SQL 查询 `medicine_taken`。这绕过了已有的 `MedicineDao.isTakenToday()` 数据源，导致：

- 数据库名称在 `ElderlyDbHelper` 和 `ReminderBroadcastReceiver` 之间重复
- 打卡查询逻辑没有唯一数据源
- 测试 `ReminderBroadcastReceiver` 需要真实 SQLite 文件

## Decision

`ReminderBroadcastReceiver` 不再直连 SQLite。改为通过 `Repository.getInstance()`（未来 ADR-0003 落地后通过 `MedicinePlanStore`）查询 `isTakenToday()`。

当 Repository 未初始化时（Receiver 在 App 启动前被 AlarmManager 触发），Receiver 在 `onReceive()` 中调用 `Repository.init(context)` 懒初始化，然后走正常路径。

## Consequences

- 删除 Receiver 中 `openOrCreateDatabase` 和手写 SQL
- `medicine_taken` 查询的唯一数据源是 `MedicineDao.isTakenToday()`
- Receiver 的单元测试可通过模拟 Repository/MedicinePlanStore 实现，不再依赖真实数据库文件
- 懒初始化 `Repository.init()` 无副作用（仅创建 DbHelper、加载数据）
