# Repository 拆分 + Module 合并 + AiChatModule 拆分

## Status

Proposed

## Context

当前 `Repository.java` 是上帝模块——41 个方法、10 个 DAO、11 个内存列表、副作用编排，接口和实现一样复杂。所有 Module、Activity、Fragment、Service 都穿过同一个接缝访问所有数据领域。

7 个 Module 类大部分是透传适配器，仅有的聚合逻辑分散在几个 Module 中。`AiChatModule` 把语音识别、API 通信、内容过滤、提示词构建四个无关关注点塞在一个文件里。

## Decisions

### 1. Repository 按领域概念拆分为独立 Store

每个领域概念对应一个 Store，持有自己的 DAO、内存列表、副作用：

| Store | 职责 | 副作用订阅/发送 |
|---|---|---|
| `UserStore` | 老人 + 家属 + 多用户切换 + 家属电话查找 | — |
| `HealthRecordStore` | 健康档案 + 蓝牙设备 | 发送 `HealthDataAdded` |
| `MedicinePlanStore` | 用药提醒 + 服药打卡 + 药品库 | 发送 `MedicineAdded`、`MedicineDeleted` |
| `AlbumStore` | 亲情相册 | — |
| `ChatStore` | 智能对话消息 | — |
| `MemoryStore` | 记忆记录 | — |
| `EmergencyStore` | 紧急告警 | — |
| `FraudTipStore` | 防诈骗推送 | — |

`AppContainer` 负责构建和注入所有 Store。

### 2. 多用户切换：SessionStore 协调

保留一个薄的 `SessionStore`，仅管理 `activeUserId` 和用户列表。用户切换时，各 Store 通过观察 `SessionStore` 各自 `reloadActive()`。不采用重建模式（销毁+重建所有 Store），避免内存抖动和引用悬空。

### 3. 副作用通过 DomainEventBus 解耦

Store 写入数据后通过 `DomainEventBus` 发布事件（`HealthDataAdded`、`MedicineAdded`、`MedicineDeleted`），`HealthAlertService` 和 `MedicineAlarmScheduler` 订阅对应事件。Store 不直接依赖 Android API。

`DomainEventBus` 是进程内事件分发器——约 30 行代码，按事件类型持有订阅者列表。Store 构造时注册。

### 4. 删除透传 Module，逻辑迁入 Store

- `HealthRecordModule`、`EmergencyModule`、`SafetyContentModule` — 纯透传，删除
- `MedicineReminderModule` 的 `getMedicines(type)`、`getTakenCount()`、`getMedicineTypes()` → `MedicinePlanStore`
- `FamilyAlbumModule` 的 `getAlbumGroups()`、`getPhotosForAlbum()` → `AlbumStore`
- `MemoryModule` 的 `getMemories(category)`、`getCategories()` → `MemoryStore`
- `UserSessionModule` 的 `getActiveUser()`、`findPrimaryFamilyPhone()` → `UserStore`

Fragment 通过 `BaseActivity`/`BaseFragment` 便捷访问器逐个获取需要的 Store，不做门面模式。

### 5. AiChatModule 拆分为薄编排器

`AiChatModule` 变为编排器，持有以下注入的适配器，通过同一个 `Listener` 接口对外：

- `VoiceInputAdapter` — 纯识别封装（`start()`、`stop()`、`destroy()`），不处理权限。权限由 `AiChatModule` 协调（因 Android 的 `onRequestPermissionsResult` 必须落在 Activity）。
- `ZhipuApiClient` — 注入而非内部 new
- `ContentFilter` — 不变
- `DrugRecommendationParser` — 不变
- `PromptBuilder` — 纯函数：`build(medicineLibrary, medicines, activeUserName) → String`

TTS 的归属留到 ADR-0006。

## Consequences

- 构建：`AppContainer` 构造量从 1 个 Repository 变为 8 个 Store，但每个 Store 职责单一
- 测试：每个 Store 可独立测试（注入假 DAO），不再需要构建全量 Repository 单例
- 导航：调用链从 `Fragment → Module → Repository → DAO` 缩短为 `Fragment → Store → DAO`
- 兼容：Fragment 的 Store 引用在用户切换时保持稳定（观察模式而非重建）
- 与 ADR-0001 的关系：仍使用 SQLiteOpenHelper + 手写 SQL，未引入 Room
