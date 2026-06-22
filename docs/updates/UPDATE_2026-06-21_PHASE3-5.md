# 银发守护者 APP 照护闭环功能扩展 — 阶段三至五更新文档

- 更新日期：2026-06-21
- 项目：Silver Guardian（银发守护者）
- 阶段：第三阶段（一键求助增强）、第四阶段（服药后身体反馈）、第五阶段（适老化操作引导）
- 需求依据：`docs/银发守护者APP_照护闭环功能扩展需求文档.md`

## 1. 更新摘要

本次更新在保留 `AppContainer → Module → Repository/DAO` 架构的前提下，完成了照护闭环功能扩展的后端逻辑部分（阶段三至五）。

主要成果包括：

- 阶段三：一键求助增强 — 自动生成结构化求助信息，整合老人姓名、健康、用药、家属联系人和告警状态。
- 阶段四：服药后身体反馈 — 新增 `medicine_feedback` 表，支持记录服药后身体感受，统计不适反馈数量，接入照护摘要。
- 阶段五：适老化操作引导 — SharedPreferences 管理引导状态，支持 9 个关键页面的首次使用引导标记与重置。

三个阶段均为纯后端逻辑/状态管理层实现，前端 UI 由 Codex 负责接入。

---

## 2. 阶段三：一键求助增强

### 2.1 新增文件

| 文件 | 说明 |
|---|---|
| `models/SosHelpInfo.java` | 求助信息结构化数据模型，含 7 个字段 + `fullMessage` |
| `modules/SosHelpModule.java` | 聚合各数据源生成求助文本的业务模块 |

### 2.2 修改的现有文件

| 文件 | 改动 |
|---|---|
| `app/AppContainer.java` | 新增 `sosHelpModule` 字段、初始化、`sosHelp()` getter，新增 1 行 import |

### 2.3 数据来源

| 字段 | 数据来源 | 获取方式 |
|---|---|---|
| `elderName` | `users` 表 | `Repository.getUsers()` + `getActiveUserId()` |
| `locationText` | 无定位模块 | 固定兜底文案 `"当前位置暂不可用"` |
| `healthStatusText` | `health_data` 表（今日） | `Repository.getTodayHealthData()` |
| `medicineStatusText` | `user_medicines` + `medicine_taken` 表 | `Repository.getMedicines()` → `takenToday` 字段 |
| `familyContactName` | `family_members` 表 | `Repository.getFamilyMembers()` → 取第一条 |
| `familyContactPhone` | `family_members` 表 | `Repository.getFamilyMembers()` → 取第一条 |
| `alertStatusText` | `emergency_alerts` + `safe_check_records` 表 | `getTodayEmergencyAlertCount()` + `getTodaySafeCheckRecord()` |

### 2.4 空数据兜底规则

| 场景 | 兜底文案 |
|---|---|
| 老人姓名为空/null | `当前老人` |
| 无定位信息 | `当前位置暂不可用` |
| 无今日健康数据 | `暂无最新健康数据` |
| 无今日用药 | `今日暂无用药计划` |
| 无家属联系人 | `暂无家属联系人，请先添加家属联系人` |
| 无告警且无未响应 | `今日暂无异常告警` |
| 各字段 null | 构造函数全部 `null → ""` 兜底 |

### 2.5 暴露给 UI 层的方法

```java
// 模块入口（从 AppContainer 获取）
appContainer().sosHelp()

// 核心方法
SosHelpInfo buildCurrentHelpInfo()
```

返回的 `SosHelpInfo` 包含：
- `elderName` — 老人姓名
- `locationText` — 位置描述
- `healthStatusText` — 健康状态（多行）
- `medicineStatusText` — 用药状态（多行）
- `familyContactName` — 家属联系人姓名
- `familyContactPhone` — 家属联系人电话
- `alertStatusText` — 异常告警状态
- `fullMessage` — 完整格式化求助文本，可直接展示/复制

### 2.6 Codex 接入指南

```java
// 在 Fragment/Activity 中获取求助信息：
SosHelpInfo info = appContainer().sosHelp().buildCurrentHelpInfo();
String text = info.fullMessage;  // 完整求助文本，可直接展示
```

---

## 3. 阶段四：服药后身体反馈

### 3.1 新增文件

| 文件 | 说明 |
|---|---|
| `models/MedicineFeedback.java` | 服药反馈数据模型，含 6 种反馈类型常量和 `isWarning()` 静态方法 |
| `data/dao/MedicineFeedbackDao.java` | 反馈表 DAO：增、查、统计不适数量、查不适列表 |
| `modules/MedicineFeedbackModule.java` | 业务模块，封装 Repository 供 UI 层调用 |

### 3.2 修改的现有文件

| 文件 | 改动 |
|---|---|
| `data/ElderlyDbHelper.java` | DB_VERSION 4→5，新增 `medicine_feedback` 表，新增 `upgradeToVersion5()` |
| `data/Repository.java` | 新增 `feedbackDao`、`medicineFeedbacks` 列表，新增 4 个公开方法 |
| `modules/CareSummaryModule.java` | `medicineFeedbackWarningCount` 从硬编码 0 改为实时查询 |
| `app/AppContainer.java` | 新增 `medicineFeedbackModule` 字段、初始化、`medicineFeedback()` getter |

### 3.3 数据库变更

**新增表 `medicine_feedback`**：

```sql
CREATE TABLE medicine_feedback (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    medicine_id INTEGER,
    medicine_name TEXT,
    feedback_type TEXT NOT NULL,
    feedback_text TEXT,
    date TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

- **DB_VERSION**：从 4 升级到 5
- **onCreate**：`createBusinessTables()` 中直接创建表（全新安装）
- **onUpgrade**：`oldVersion < 5` 时调用 `upgradeToVersion5(db)`，`CREATE TABLE IF NOT EXISTS`（从 v1/v2/v3/v4 升级安全，不丢数据）
- **种子数据**：未修改 `seedDefaultTemplates()`

### 3.4 反馈类型常量

定义在 `MedicineFeedback` 中：

| 常量 | 含义 | 是否算不适 |
|---|---|---|
| `TYPE_NORMAL` | 感觉正常 | 否 |
| `TYPE_DIZZY` | 有点头晕 | **是** |
| `TYPE_NAUSEA` | 有点恶心 | **是** |
| `TYPE_PALPITATION` | 心慌不舒服 | **是** |
| `TYPE_OTHER` | 其他不适 | **是** |
| `TYPE_SKIPPED` | 暂不填写 | 否 |

静态方法 `MedicineFeedback.isWarning(String feedbackType)` 判断是否为不适反馈。

### 3.5 暴露给 UI 层的方法

```java
// 模块入口
appContainer().medicineFeedback()

// 保存反馈（打卡后调用）
MedicineFeedback addFeedback(int medicineId, String medicineName, String feedbackType, String feedbackText)

// 获取今日所有反馈
List<MedicineFeedback> getTodayFeedbacks()

// 获取今日不适反馈数量
int getTodayWarningCount()

// 获取今日不适反馈列表
List<MedicineFeedback> getTodayWarnings()
```

### 3.6 照护摘要集成

`CareSummaryModule.getTodaySummary()` 现在从 Repository 实时获取 `medicineFeedbackWarningCount`：

- `warningCount > 0` → `"服药反馈：有 N 条不适反馈"`
- `warningCount == 0` → `"服药反馈：暂无反馈"`

### 3.7 Codex 接入指南

```java
// 打卡成功后弹出反馈弹窗，选择后保存：
appContainer().medicineFeedback().addFeedback(
    medicine.id, medicine.name,
    MedicineFeedback.TYPE_DIZZY,  // 或 TYPE_NORMAL / TYPE_SKIPPED 等
    ""  // feedbackText，可为空
);

// 照护摘要页面获取不适数量：
int count = appContainer().medicineFeedback().getTodayWarningCount();
```

---

## 4. 阶段五：适老化操作引导

### 4.1 新增文件

| 文件 | 说明 |
|---|---|
| `modules/SeniorGuideModule.java` | 基于 SharedPreferences 的引导状态管理器 |

### 4.2 修改的现有文件

| 文件 | 改动 |
|---|---|
| `app/AppContainer.java` | 新增 `seniorGuideModule` 字段、初始化、`seniorGuide()` getter |

### 4.3 技术方案

- **存储方式**：SharedPreferences，文件名 `senior_guides`
- **数据隔离**：当前版本所有用户共用引导状态（不按 userId 隔离，保持简单）
- **默认行为**：所有 key 首次查询返回 true（未展示），`markShown` 后返回 false

### 4.4 支持的引导 Key

| 常量 | SharedPreferences Key | 适用页面 |
|---|---|---|
| `GUIDE_HOME` | `guide_home_shown` | 首页 |
| `GUIDE_MEDICINE` | `guide_medicine_shown` | 用药提醒页 |
| `GUIDE_FAMILY_MANAGE` | `guide_family_manage_shown` | 家属联系人管理 |
| `GUIDE_ALBUM` | `guide_album_shown` | 家人相册页 |
| `GUIDE_FRAUD` | `guide_fraud_shown` | 防诈提醒页 |
| `GUIDE_SAFE_CHECK` | `guide_safe_check_shown` | 每日平安确认 |
| `GUIDE_SOS` | `guide_sos_shown` | 一键求助 / SOS |
| `GUIDE_CARE_SUMMARY` | `guide_care_summary_shown` | 照护摘要页 |
| `GUIDE_MEDICINE_FEEDBACK` | `guide_medicine_feedback_shown` | 服药后身体反馈 |

### 4.5 暴露给 UI 层的方法

```java
// 模块入口
appContainer().seniorGuide()

// 判断是否需要展示引导（首次返回 true）
boolean shouldShow(String key)

// 标记引导已展示（弹窗关闭后调用）
void markShown(String key)

// 查询是否已展示
boolean hasShown(String key)

// 重置单个页面引导
void reset(String key)

// 重置全部引导（设置页"重新查看操作引导"调用）
void resetAll()
```

### 4.6 Codex 接入指南

**页面端（以首页为例）**：
```java
SeniorGuideModule guide = appContainer().seniorGuide();
if (guide.shouldShow(SeniorGuideModule.GUIDE_HOME)) {
    // 弹出引导 AlertDialog
    // 用户点击"我知道了"后：
    guide.markShown(SeniorGuideModule.GUIDE_HOME);
}
```

**设置页重置**：
```java
appContainer().seniorGuide().resetAll();
Toast.makeText(context, "操作引导已重置", Toast.LENGTH_SHORT).show();
```

### 4.7 未修改数据库

本阶段**没有修改数据库**，未改动 `ElderlyDbHelper.java`、`DB_VERSION`、`onCreate`、`onUpgrade` 和任何种子数据。

---

## 5. AppContainer 当前完整模块一览

```java
// AppContainer 当前管理的所有模块（按初始化顺序）：
userSessionModule      // UserSessionModule  — 用户与家属联系人
healthRecordModule     // HealthRecordModule — 健康档案
medicineReminderModule // MedicineReminderModule — 用药提醒
medicineFeedbackModule // MedicineFeedbackModule — 服药后身体反馈 [阶段四]
familyAlbumModule      // FamilyAlbumModule — 家人相册
memoryModule           // MemoryModule — 记忆回忆
safeCheckModule        // SafeCheckModule — 每日平安确认
careSummaryModule      // CareSummaryModule — 照护摘要
emergencyModule        // EmergencyModule — 紧急求助
safetyContentModule    // SafetyContentModule — 安全内容
sosHelpModule          // SosHelpModule — 一键求助增强 [阶段三]
seniorGuideModule      // SeniorGuideModule — 适老化操作引导 [阶段五]
communityPoiSearchModule // CommunityPoiSearchModule — 便民查询
aiChatModule           // AiChatModule — AI 对话
weatherModule          // WeatherModule — 天气出行
```

---

## 6. 数据库版本历史

| DB_VERSION | 新增内容 | 对应阶段 |
|---|---|---|
| 1 | 初始版本：users, messages, health_data, memories, reminders, family_members, user_medicines, medicine_taken, album_photos, emergency_alerts | 初始 |
| 2 | users 增加 hint_question/hint_answer；业务表重建隔离；新增 medicine_library, fraud_tips | 课程新需求 |
| 3 | 新增 albums 表；album_photos 增加 album_id | 相册重构 |
| 4 | 新增 safe_check_records 表 + 索引 | 每日平安确认 |
| **5** | **新增 medicine_feedback 表** | **阶段四：服药后身体反馈** |

---

## 7. 测试方法

### 7.1 一键求助测试

```java
Repository repo = Repository.getInstance();
SosHelpModule module = new SosHelpModule(repo);
SosHelpInfo info = module.buildCurrentHelpInfo();
System.out.println(info.fullMessage);
```

### 7.2 服药反馈测试

```java
Repository repo = Repository.getInstance();

// 保存一条反馈
MedicineFeedback fb = repo.addMedicineFeedback(
    1, "硝苯地平", MedicineFeedback.TYPE_PALPITATION, "吃完有点心慌");

// 读取今日反馈
List<MedicineFeedback> all = repo.getTodayMedicineFeedbacks();
System.out.println("今日反馈数: " + all.size());

// 读取不适数量
int warnings = repo.getTodayMedicineFeedbackWarningCount();
System.out.println("不适反馈数: " + warnings);

// 验证 isWarning
System.out.println("NORMAL isWarning: " + MedicineFeedback.isWarning("NORMAL"));    // false
System.out.println("DIZZY isWarning: " + MedicineFeedback.isWarning("DIZZY"));      // true
```

### 7.3 引导状态测试

```java
// 在任意 Activity 中
SeniorGuideModule guide = appContainer().seniorGuide();

// 首次应返回 true
System.out.println("首页应显示引导: " + guide.shouldShow(SeniorGuideModule.GUIDE_HOME));

// 标记已展示
guide.markShown(SeniorGuideModule.GUIDE_HOME);

// 再次查询应返回 false
System.out.println("首页已展示: " + guide.hasShown(SeniorGuideModule.GUIDE_HOME));
System.out.println("首页应显示引导: " + guide.shouldShow(SeniorGuideModule.GUIDE_HOME));

// 重置全部
guide.resetAll();

// 再次查询应返回 true
System.out.println("重置后应显示: " + guide.shouldShow(SeniorGuideModule.GUIDE_HOME));
```

---

## 8. 关键文件索引

### 阶段三：一键求助增强
- `app/src/main/java/com/silverguardian/prototype/models/SosHelpInfo.java`
- `app/src/main/java/com/silverguardian/prototype/modules/SosHelpModule.java`
- `app/src/main/java/com/silverguardian/prototype/app/AppContainer.java`（已修改）

### 阶段四：服药后身体反馈
- `app/src/main/java/com/silverguardian/prototype/models/MedicineFeedback.java`
- `app/src/main/java/com/silverguardian/prototype/data/dao/MedicineFeedbackDao.java`
- `app/src/main/java/com/silverguardian/prototype/modules/MedicineFeedbackModule.java`
- `app/src/main/java/com/silverguardian/prototype/data/ElderlyDbHelper.java`（已修改，DB_VERSION=5）
- `app/src/main/java/com/silverguardian/prototype/data/Repository.java`（已修改）
- `app/src/main/java/com/silverguardian/prototype/modules/CareSummaryModule.java`（已修改）
- `app/src/main/java/com/silverguardian/prototype/app/AppContainer.java`（已修改）

### 阶段五：适老化操作引导
- `app/src/main/java/com/silverguardian/prototype/modules/SeniorGuideModule.java`
- `app/src/main/java/com/silverguardian/prototype/app/AppContainer.java`（已修改）

---

## 9. Codex 后续待完成事项

1. **一键求助 UI**：在 SOS 弹窗或一键求助入口中调用 `sosHelp().buildCurrentHelpInfo()`，展示 + 复制 + 拨号按钮。
2. **服药反馈弹窗**：在"已服药"打卡成功后弹出反馈选择弹窗，调用 `medicineFeedback().addFeedback()`。
3. **照护摘要展示反馈**：`CareSummary` 已包含 `medicineFeedbackWarningCount`，UI 可直接读取展示。
4. **9 个页面引导弹窗**：在对应页面 `onCreateView`/`onResume` 中检查 `seniorGuide().shouldShow(key)`，弹出 AlertDialog。
5. **设置页重置入口**：新增"重新查看操作引导"按钮，点击调用 `seniorGuide().resetAll()` + Toast 提示。
6. **BaseFragment 补充**：建议在 `BaseFragment` 中新增：
   ```java
   protected MedicineFeedbackModule medicineFeedback() { return appContainer().medicineFeedback(); }
   protected SosHelpModule sosHelp() { return appContainer().sosHelp(); }
   protected SeniorGuideModule seniorGuide() { return appContainer().seniorGuide(); }
   ```

---

## 10. 重要限制说明

- 阶段三未接入真实定位，`locationText` 固定返回兜底文案，后续可通过覆写 `buildLocationText()` 接入定位模块。
- 阶段四未破坏现有用药提醒、闹钟、打卡、删除药品功能。
- 阶段五当前所有用户共用引导状态（不按 userId 隔离），必要时可在 `SeniorGuideModule` 中增加 userId 前缀。
- 三个阶段均未修改 `strings.xml`，所有中文文案均为 Java 字面量或由 Codex 前端处理。
- 三个阶段均未修改 `ElderlyDbHelper.java` 的种子数据。
- `DB_VERSION` 为 5，从 1/2/3/4 升级均通过 `onUpgrade` 的链式升级路径安全迁移。
