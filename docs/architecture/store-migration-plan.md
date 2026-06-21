# Repository / Store 渐进式迁移计划

> 对应 ADR-0003。本文件只定义迁移顺序与验收标准，本次不创建 Store、不删除 Repository、不修改页面访问器，也不落地 DomainEventBus。

## 1. 当前 Repository 职责拆分表

| 当前职责 | 当前依赖/状态 | 目标 Store |
|---|---|---|
| 老人档案 CRUD、默认模板初始化 | `UserDao`、`ElderlyDbHelper.seedDefaultTemplates()` | `UserStore` |
| 家属联系人 CRUD | `FamilyDao` | `UserStore` |
| activeUserId 持久化与切换 | `SharedPreferences`，切换后全量 reload | `SessionStore` |
| 健康档案读取、今日概览、写入、蓝牙模拟设备 | `HealthDao`、内存列表、`HealthAlertService` 副作用 | `HealthRecordStore` |
| 用药计划、服药打卡、药品库 | `MedicineDao`、`MedicineLibraryDao` | `MedicinePlanStore` |
| 用药闹钟安排/取消 | `MedicineAlarmScheduler` 副作用 | 后续事件订阅器 |
| 亲情相册 CRUD、收藏、分类 | `AlbumDao`、`PhotoStorage` 文件副作用 | `AlbumStore` |
| 智能对话历史与重置 | `ChatDao` | `ChatStore` |
| 记忆记录 CRUD、分类 | `MemoryDao` | `MemoryStore` |
| 紧急告警记录 | `EmergencyDao` | `EmergencyStore` |
| 防诈骗内容读取、分类 | `FraudTipDao` | `FraudTipStore` |
| 所有领域的内存列表与 reload | Repository 统一持有 | 各 Store 各自持有并按老人重载 |

## 2. Store 持有的 DAO 与建议接口

### UserStore

- DAO：`UserDao`、`FamilyDao`；创建老人时可通过一个内部初始化适配器调用默认模板初始化。
- 接口：`getUsers()`、`findUser(userId)`、`addUser(...)`、`deleteUser(userId)`、`getFamilyMembers(userId)`、`addFamilyMember(...)`、`updateFamilyMember(...)`、`deleteFamilyMember(...)`。
- 不负责 activeUserId；调用方显式传 userId，避免隐藏会话状态。

### SessionStore

- DAO：无；持有 `SharedPreferences`。
- 接口：`getActiveUserId()`、`setActiveUserId(userId)`、`addListener/removeListener`（仅在需要第二个订阅者时引入）。
- 只管理当前老人身份，不持有领域数据，不销毁重建其他 Store。

### HealthRecordStore

- DAO：`HealthDao`。
- 接口：`load(userId)`、`getHealthRecords()`、`getTodayHealthRecords(userId)`、`addHealthRecord(userId, type, value, status, notes)`、`getBluetoothDevices()`。
- `HealthMetricEvaluator` 仍是评估接口；健康告警副作用后续从写入流程迁出。

### MedicinePlanStore

- DAO：`MedicineDao`、`MedicineLibraryDao`。
- 接口：`load(userId, today)`、`getMedicines()`、`addMedicine(...)`、`deleteMedicine(...)`、`setTaken(...)`、`isTakenToday(userId, medicineId)`、`searchLibrary(...)`、`add/update/deleteLibraryItem(...)`、`getMedicineTypes()`。
- Repository 当前新增的 `isMedicineTakenToday()` 最先迁到这里，Receiver 再改依赖此 Store。

### AlbumStore

- DAO：`AlbumDao`。
- 接口：`load(userId)`、`getPhotos()`、`addPhoto(...)`、`deletePhoto(...)`、`setFavorite(...)`、`getCategories()`。
- `PhotoStorage` 先作为内部适配器保留；删除文件与删除数据库记录的失败顺序需单独测试。

### ChatStore

- DAO：`ChatDao`。
- 接口：`load(userId)`、`getMessages()`、`addMessage(userId, text, type)`、`resetSession(userId, welcome)`。

### MemoryStore

- DAO：`MemoryDao`。
- 接口：`load(userId)`、`getMemories()`、`addMemory(...)`、`deleteMemory(...)`、`getCategories()`。

### EmergencyStore

- DAO：`EmergencyDao`。
- 接口：`load(userId)`、`getAlerts()`、`addAlert(userId, message)`。

### FraudTipStore

- DAO：`FraudTipDao`。
- 接口：`load(userId)`、`getFraudTips()`、`getCategories()`。

## 3. 保留为兼容门面的 Module

第一轮全部保留：`UserSessionModule`、`HealthRecordModule`、`MedicineReminderModule`、`FamilyAlbumModule`、`MemoryModule`、`EmergencyModule`、`SafetyContentModule`。它们先从 Repository 适配器改为对应 Store 适配器，页面接口不变。

`UserSessionModule` 在过渡期组合 `UserStore + SessionStore`；其余 Module 原则上一对一适配 Store。`BaseFragment`、`BaseActivity` 继续暴露现有 Module，直到 Store 经测试稳定。只有当某个 Module 已无调用者且通过删除测试确认不会把复杂度撒回页面层时，才单独 PR 删除。

## 4. 后续迁往 DomainEventBus 的副作用

| 事件 | 发布方 | 订阅方 |
|---|---|---|
| `HealthRecordAdded`（含 `EvaluationResult`） | `HealthRecordStore` 成功写入后 | `HealthAlertService` |
| `MedicineAdded` | `MedicinePlanStore` 成功写入后 | `MedicineAlarmScheduler` |
| `MedicineDeleted` | `MedicinePlanStore` 成功删除后 | `MedicineAlarmScheduler` |
| `ActiveUserChanged` | `SessionStore` | 各 Store 的 reload 协调器 |
| `AlbumPhotoDeleted`（可选） | `AlbumStore` | 图片文件清理适配器 |

DomainEventBus 本次不实现。落地时要求同步分发、明确异常策略，并保证“数据库提交成功后再发布”；否则事件会把一致性问题藏起来。

## 5. 推荐 PR 顺序与验收标准

### PR-1：Store 基础约定与 SessionStore

- 新增 Store 包结构、`SessionStore`，由 `AppContainer` 构建。
- `UserSessionModule` 仅将 activeUserId 读写切到 SessionStore，用户/家属仍走 Repository。
- 验收：切换老人后 ID 可持久化；重启恢复当前老人；页面访问器不变；编译和会话测试通过。

### PR-2：MedicinePlanStore 纵向切片

- 迁移 `MedicineDao + MedicineLibraryDao`、今日打卡查询和内存列表。
- `MedicineReminderModule`、`ReminderBroadcastReceiver` 改用 Store；Repository 同名方法暂时委托 Store。
- 闹钟副作用暂留在兼容编排层，不引入事件总线。
- 验收：新增/删除药品、药品库 CRUD、打卡、已打卡不再提醒、重复提醒、通知点击均正常；无 Receiver 直连 DAO/SQLite。

### PR-3：HealthRecordStore 纵向切片

- 迁移 `HealthDao`、健康档案列表与蓝牙模拟设备。
- `HealthRecordModule` 改为 Store 适配器；Repository 暂委托 Store。
- 告警调用暂留兼容编排层。
- 验收：今日概览、手工录入、状态标签和异常告警一致；切换老人无数据串线；评估单元测试和编译通过。

### PR-4：UserStore

- 迁移 `UserDao + FamilyDao` 及新老人默认模板事务。
- `UserSessionModule` 组合 UserStore 与 SessionStore。
- 验收：老人和家属 CRUD 正常；至少保留一个老人规则不变；新增老人模板完整；多用户隔离通过。

### PR-5：低耦合 Store 批次

- 分两个小提交迁移 `MemoryStore + FraudTipStore`，再迁移 `EmergencyStore + ChatStore`。
- 对应 Module 与 AiChatModule 只改数据依赖，不拆 AI 其他职责。
- 验收：各领域 CRUD/重置行为一致；按老人隔离；原 Module 接口不变；每个提交独立编译。

### PR-6：AlbumStore

- 迁移 `AlbumDao` 与相册列表；将 `PhotoStorage` 作为 Store 内部适配器。
- 验收：上传、收藏、单张删除、整相册删除、私有文件与公共 URI 清理均正常；失败时不留下错误内存状态。

### PR-7：Repository 收缩与 AppContainer 收口

- Repository 仅作临时兼容门面并委托 Store；删除已无调用的 DAO、列表和 reload 职责。
- 验收：Repository 不再直接持有已迁移 DAO；AppContainer 是唯一组合根；全量搜索确认页面未绕过既定 seam；全量测试/编译通过。

### PR-8：页面访问器与纯透传 Module 清理

- 每次只迁一个页面族，从 Module 改为 Store；随后删除确认无使用者的纯透传 Module。
- 验收：`BaseFragment`/`BaseActivity` 每次只减少已完成领域的访问器；删除测试通过；无一次性全页面修改。

### PR-9：DomainEventBus（独立决策后）

- 在另一个 ADR 明确同步/异步、错误传播、线程和生命周期后再实现。
- 验收：健康写入与告警、用药写入与调度可分别测试；事件只在事务成功后发布；订阅异常不会破坏数据一致性。

## 6. 明确不在本次计划中直接落地的内容

- 不删除 Repository 或任何现有 Module。
- 不修改数据库结构，不引入 Room。
- 不批量修改 Fragment、Activity、BaseFragment、BaseActivity。
- 不实现 DomainEventBus。
- 不继续拆分 AiChatModule 的语音输入、网络、提示词和解析职责；本次只保留已完成的 TtsAdapter seam。