# 统一健康评估逻辑

## Status

Proposed

## Context

`HealthMetricEvaluator.status()` 和 `HealthAlertService.evaluate()` 各自解析相同的健康档案指标类型（心率、血压、血氧、体温、血糖、呼吸率）并应用阈值判断。两者的"正常边界"一致，但：

- `HealthMetricEvaluator` 返回 2 级（正常 / 异常），用于 UI 显示
- `HealthAlertService` 返回 3 级（NORMAL / WARNING / CRITICAL），用于告警决策

阈值逻辑分散在两个模块中，新增指标类型需要改两处。

## Decision

`HealthMetricEvaluator` 成为阈值逻辑的唯一数据源。`status()` 改为返回 `EvaluationResult`（包含两个字段）：

- `displayLabel: String` — 显示标签（"正常"、"偏高"、"偏低"、"需关注"）
- `alertLevel: AlertLevel` — 告警级别（NORMAL / WARNING / CRITICAL）

`HealthAlertService` 不再包含任何 `switch(type)` 阈值判断。它只接收 `EvaluationResult`，当 `alertLevel` 为 WARNING 或 CRITICAL 时触发通知。

## Consequences

- 删除 `HealthAlertService.evaluate()` 方法及其 switch 语句
- 新增指标类型只需修改 `HealthMetricEvaluator`
- `HealthAlertService` 可独立于阈值逻辑测试——注入假 `EvaluationResult` 验证通知行为
- 与 ADR-0003 的 `DomainEventBus` 一致：`HealthDataAdded` 事件携带 `EvaluationResult`
