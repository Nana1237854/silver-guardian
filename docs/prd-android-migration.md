# PRD: 银发守护者 Android 原生移植

## Problem Statement

银发守护者当前是 Vue 3 + Vite Web 应用，仅可在 PC 浏览器访问。老人用户群体普遍不熟悉电脑操作，且移动场景下（外出散步、就医等）无法使用。课程要求使用 Android Studio 原生开发，因此需要将全部功能移植为一款完全独立的 Android 原生 App。

## Solution

开发一款完全独立的 Android 原生应用，取代现有的 Vue Web 前端和 Express 后端。所有数据本地存储（SQLite），AI 对话直连智谱 GLM-4 API。目标用户为 60 岁以上老人及其家属，界面采用大字体、大按钮、高对比度设计。

## User Stories

### 用户登录与切换
1. 作为老人，我希望通过输入 4 位 PIN 码快速登录，无需记住复杂密码，以便无障碍使用 App
2. 作为老人，我希望在登录页看到所有已注册用户的头像列表，点击选择后再输入 PIN，以便我和配偶共用同一台手机
3. 作为老人，我希望能添加新用户（输入姓名、选择头像、设置 PIN），以便新成员加入使用
4. 作为老人，我希望能删除不再使用的用户账户，以便清理数据
5. 作为老人，我希望在登录状态下可以一键退出并切换用户，以便家人临时使用我的手机

### 首页中枢
6. 作为老人，我希望打开 App 后看到今日日期、天气信息，以便对当天有基本认知
7. 作为老人，我希望首页展示我的健康快报（心率、步数、血压、血糖等关键指标），以便无需进入深层页面就能了解身体状况
8. 作为老人，我希望首页一眼看到 SOS 紧急呼叫按钮（红色醒目），以便紧急情况下快速求助
9. 作为老人，我希望首页展示家属在线状态列表，以便我知道谁此刻可以联系
10. 作为老人，我希望可以从首页直接进入与某位家属的对话，以便快速沟通

### AI 智能对话
11. 作为老人，我希望用语音或文字向智能助手提问健康相关问题，以便获得即时健康建议
12. 作为老人，当我在对话中提到疾病时，我希望助手推荐 2-3 种对应药品（含药品图片和说明），以便我了解用药选择
13. 作为老人，当我提出不适合老年人的行为（如爬10公里、搬重物）时，我希望助手温和地劝解并建议安全替代方案，以便保护我的健康
14. 作为老人，我希望看到对话历史记录，以便回顾之前的健康建议
15. 作为老人，我希望一键清空对话历史，以便重新开始新话题

### 健康档案
16. 作为老人，我希望手动输入健康指标数据（血压、血糖、心率、步数、血氧、体温、体重、体脂率、呼吸率），以便长期追踪健康状况
17. 作为老人，我希望按类型和时间查看所有健康指标的历史记录列表，以便了解趋势
18. 作为老人，我希望通过蓝牙连接血压计等设备自动上传数据，以便减少手动输入（创新点）
19. 作为老人，我希望可以删除误录的健康数据，以便保持数据准确

### 用药提醒与打卡
20. 作为老人，我希望添加药品信息（名称、类型、服用时间、用法、说明、图片），以便建立个人用药计划
21. 作为老人，我希望在设定时间收到系统的用药提醒通知，以便不会漏服药品
22. 作为老人，我希望收到通知后点击完成服药打卡，以便记录当天的服药情况
23. 作为老人，我希望查看今天的打卡完成情况（已打卡/未打卡的药品列表），以便确认是否有遗漏
24. 作为老人，我希望能删除不再服用的药品，以便管理当前用药
25. 作为老人，我希望查看药品库（疾病→药品映射），以便了解常见疾病对应哪些药物
26. 作为老人，我希望手机重启后用药提醒能自动恢复，以便不因重启手机错过提醒

### 亲情相册
27. 作为老人，我希望浏览家属为我上传的照片墙，以便回忆美好时光
28. 作为老人，我希望能放大查看单张照片，以便看清楚细节
29. 作为老人，我希望能收藏特别喜欢的照片，以便快速找到
30. 作为老人，我希望按分类（家庭、旅行、日常等）筛选照片，以便快速定位
31. 作为老人，我希望看到每张照片附带的家属留言，以便感受亲人关怀

### 家属管理 & 子女模式
32. 作为家属，我希望被添加到老人的家属列表中（姓名、关系、电话、头像），以便老人能看到我
33. 作为家属，我希望切换到子女模式查看老人的健康数据摘要，以便远程关注老人健康
34. 作为家属，我希望在子女模式中看到老人的服药打卡状态，以便知道老人是否按时服药
35. 作为家属，我希望在子女模式中看到 SOS 紧急提醒记录，以便及时响应
36. 作为家属，我希望能为老人上传照片到亲情相册，并附上场景描述和留言，以便老人感受陪伴

### SOS 紧急呼叫
37. 作为老人，我在任何页面都可以快速触发 SOS 模式，以便紧急情况下不被界面层级阻碍
38. 作为老人，触发 SOS 后我希望看到预置的紧急联系选项和语音提示，以便采取下一步行动

### 记忆与回忆
39. 作为老人，我希望记录重要的生活记忆（分类为健康、爱好、家庭等），以便回顾
40. 作为老人，我希望可以删除不再需要的记忆记录

### 防诈骗与社区
41. 作为老人，我希望看到系统展示的防诈骗提示卡片，以便提高安全意识
42. 作为老人，我希望查看防诈骗提示的分类列表，以便了解不同类型的诈骗手段

### 全局体验
43. 作为老人，我希望 App 的字体足够大、按钮足够大、对比度足够高，以便视力不佳也能正常使用
44. 作为老人，我希望关键操作有明确的确认弹窗（如删除），以便避免误操作

## Implementation Decisions

### 模块划分

| 模块 | 职责 | 接口 |
|------|------|------|
| `db` | SQLiteOpenHelper + 11 张表的 CRUD DAO | `ElderlyDbHelper(context)`, 每个 DAO 暴露标准增删改查方法 |
| `ui-login` | LoginActivity — PIN 登录、用户列表、添加/删除用户 | Intent 启动 MainActivity |
| `ui-main` | MainActivity — DrawerLayout + 4 个 Fragment 容器 | 管理 Fragment 切换、Toolbar 状态 |
| `ui-home` | HomeFragment — 健康快报卡片、家属列表、快捷入口 | 通过接口回调跳转到其他 Fragment/Activity |
| `ui-health` | HealthFragment — 指标记录列表 + 添加/编辑弹窗 | 依赖 `db.HealthDataDao` |
| `ui-medicine` | MedicineFragment — 药品列表、打卡、提醒管理 | 依赖 `db.MedicineDao`、依赖 `reminder` 模块 |
| `ui-album` | AlbumFragment + PhotoDetailActivity — 照片网格、上传、收藏 | 依赖 `db.AlbumDao`、依赖 Glide |
| `ui-child` | ChildModeActivity — 子女端健康数据汇总面板 | 依赖多个 DAO 查询 |
| `ui-chat` | ChatDetailActivity — 对话列表 + 输入栏 + 语音按钮 | 依赖 `network.ZhipuApiClient`、依赖 `db.MessageDao` |
| `ui-bluetooth` | BluetoothActivity — BLE 扫描、连接、数据读取 | 依赖 `db.HealthDataDao` |
| `network` | ZhipuApiClient — 封装对智谱 GLM-4 /v4/chat/completions 的 HTTP POST | `sendMessage(prompt, callback)` 回调返回 AI 回复文本 |
| `reminder` | MedicineReminderService + ReminderBroadcastReceiver + BootReceiver | 通过 AlarmManager + NotificationManager 触发 |
| `voice` | SpeechRecognizer — 语音转文字 | `startListening(callback)` 回调返回识别文本 |
| `util` | JSON 读取、图片文件管理、日期格式化、SharedPreferences 封装 | 全项目使用 |

### 模块依赖关系

```
ui-login ──▶ ui-main
ui-main ──▶ ui-home / ui-health / ui-medicine / ui-album
ui-home ──▶ ui-chat / ui-child
ui-chat ──▶ network
ui-medicine ──▶ reminder
ui-bluetooth ──▶ db
所有 ui-* 模块 ──▶ db
所有模块 ──▶ util
```

### 数据库设计

11 张 SQLite 表（10 张来自原 MySQL + 1 张新增药品库）。每个 DAO 类对应 1 张表，提供标准 CRUD 方法。`ElderlyDbHelper` 在 `onCreate()` 中执行建表 + 从 assets 导入药品库数据。

数据库名: `elderly_guardian.db`，版本号: `1`。

#### 表 1: users（老人用户）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 用户 ID |
| `name` | TEXT | NOT NULL | 用户姓名，1-10 个字符 |
| `pin` | TEXT | NOT NULL | 4 位数字 PIN 码 |
| `avatar` | TEXT | | 头像路径，本地文件路径或空（空则使用默认头像） |
| `age` | INTEGER | | 年龄，1-150 |
| `health_conditions` | TEXT | | 健康状况描述，如"高血压、糖尿病" |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间，ISO8601 格式 |

建表 SQL:
```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  pin TEXT NOT NULL,
  avatar TEXT,
  age INTEGER,
  health_conditions TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);
```

#### 表 2: messages（AI 对话历史）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 消息 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `content` | TEXT | NOT NULL | 消息正文 |
| `type` | TEXT | NOT NULL | 消息类型: `"user"` 或 `"ai"` |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 发送时间 |

建表 SQL:
```sql
CREATE TABLE messages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  content TEXT NOT NULL,
  type TEXT NOT NULL CHECK(type IN ('user','ai')),
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 3: health_data（健康指标记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 记录 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `type` | TEXT | NOT NULL | 指标类型: `"blood_pressure"` / `"blood_sugar"` / `"heart_rate"` / `"steps"` / `"blood_oxygen"` / `"temperature"` / `"weight"` / `"body_fat"` / `"respiratory_rate"` / `"calories"` |
| `value` | TEXT | NOT NULL | 指标值，如 `"120/80"`（血压）、`"5.6"`（血糖） |
| `notes` | TEXT | | 备注 |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 记录时间 |

建表 SQL:
```sql
CREATE TABLE health_data (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  type TEXT NOT NULL,
  value TEXT NOT NULL,
  notes TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 4: memories（记忆记录）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 记忆 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `memory` | TEXT | NOT NULL | 记忆内容 |
| `category` | TEXT | | 分类: `"health"` / `"hobby"` / `"family"` / `"other"` |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间 |

建表 SQL:
```sql
CREATE TABLE memories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  memory TEXT NOT NULL,
  category TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 5: reminders（用药提醒计划）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 提醒 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `message` | TEXT | NOT NULL | 提醒文字，如 "该吃降压药了" |
| `reminder_time` | TEXT | NOT NULL | 提醒时间，格式 `"HH:mm"` |
| `triggered` | INTEGER | NOT NULL DEFAULT 0 | 是否已触发: 0=未触发, 1=已触发 |
| `reminder_type` | TEXT | DEFAULT 'medicine' | 提醒类型: `"medicine"` / `"walk"` / `"other"` |
| `description` | TEXT | | 补充说明 |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间 |

建表 SQL:
```sql
CREATE TABLE reminders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  message TEXT NOT NULL,
  reminder_time TEXT NOT NULL,
  triggered INTEGER NOT NULL DEFAULT 0,
  reminder_type TEXT DEFAULT 'medicine',
  description TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 6: family_members（家属信息）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 家属 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `name` | TEXT | NOT NULL | 家属姓名，1-10 个字符 |
| `relationship` | TEXT | NOT NULL | 关系: `"儿子"` / `"女儿"` / `"配偶"` / `"孙子"` / `"孙女"` 等 |
| `phone` | TEXT | | 电话号码 |
| `avatar` | TEXT | | 头像路径，本地文件路径或空 |
| `status` | TEXT | DEFAULT 'offline' | 在线状态: `"online"` / `"offline"` |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间 |

建表 SQL:
```sql
CREATE TABLE family_members (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  relationship TEXT NOT NULL,
  phone TEXT,
  avatar TEXT,
  status TEXT DEFAULT 'offline',
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 7: user_medicines（用户药品）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 药品 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `name` | TEXT | NOT NULL | 药品名称 |
| `type` | TEXT | | 药品类型: `"西药"` / `"中药"` / `"保健品"` |
| `time` | TEXT | | 服用时间，如 `"08:00,20:00"`（多个时间用逗号分隔） |
| `method` | TEXT | | 用法: `"口服"` / `"外用"` / `"注射"` |
| `description` | TEXT | | 说明，如 "饭后服用，每次1片" |
| `image` | TEXT | | 药品图片本地路径 |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间 |

建表 SQL:
```sql
CREATE TABLE user_medicines (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  type TEXT,
  time TEXT,
  method TEXT,
  description TEXT,
  image TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 8: medicine_taken（服药打卡）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 打卡 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `medicine_id` | INTEGER | NOT NULL, FOREIGN KEY → user_medicines(id) | 药品 ID |
| `taken_date` | TEXT | NOT NULL | 打卡日期，格式 `"yyyy-MM-dd"` |
| `taken_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 打卡时间 |

约束: `UNIQUE(user_id, medicine_id, taken_date)` — 同一用户、同一药品、同一天只能打卡一次。

建表 SQL:
```sql
CREATE TABLE medicine_taken (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  medicine_id INTEGER NOT NULL,
  taken_date TEXT NOT NULL,
  taken_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  UNIQUE(user_id, medicine_id, taken_date),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (medicine_id) REFERENCES user_medicines(id) ON DELETE CASCADE
);
```

#### 表 9: album_photos（亲情相册）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 照片 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `url` | TEXT | NOT NULL | 照片本地路径 |
| `title` | TEXT | NOT NULL | 照片标题，1-50 个字符 |
| `description` | TEXT | | 照片描述 |
| `category` | TEXT | DEFAULT 'family' | 分类: `"family"` / `"travel"` / `"daily"` / `"festival"` / `"other"` |
| `uploaded_by` | TEXT | DEFAULT '家属' | 上传者 |
| `favorite` | INTEGER | DEFAULT 0 | 收藏标记: 0=未收藏, 1=已收藏 |
| `scene_tag` | TEXT | | 场景标签，如 "春节团聚" |
| `family_message` | TEXT | | 家属留言 |
| `uploaded_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 上传时间 |

建表 SQL:
```sql
CREATE TABLE album_photos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  url TEXT NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  category TEXT DEFAULT 'family',
  uploaded_by TEXT DEFAULT '家属',
  favorite INTEGER DEFAULT 0,
  scene_tag TEXT,
  family_message TEXT,
  uploaded_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 10: emergency_alerts（SOS 紧急提醒）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 提醒 ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY → users(id) | 所属老人 |
| `keyword` | TEXT | NOT NULL | 触发关键词，如 `"救命"`、`"摔倒"` |
| `message` | TEXT | NOT NULL | 提醒内容 |
| `created_at` | TEXT | NOT NULL DEFAULT (datetime('now','localtime')) | 创建时间 |

建表 SQL:
```sql
CREATE TABLE emergency_alerts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  keyword TEXT NOT NULL,
  message TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 表 11: medicine_library（药品库，新增）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 药品 ID |
| `disease` | TEXT | NOT NULL | 疾病名称，如 `"高血压"`、`"糖尿病"` |
| `medicine_name` | TEXT | NOT NULL | 药品通用名，如 `"硝苯地平缓释片"` |
| `brand` | TEXT | | 商品名，如 `"拜新同"` |
| `description` | TEXT | | 药品说明 |
| `image` | TEXT | | 药品图片本地路径（assets 内或 res 内） |

建表 SQL:
```sql
CREATE TABLE medicine_library (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  disease TEXT NOT NULL,
  medicine_name TEXT NOT NULL,
  brand TEXT,
  description TEXT,
  image TEXT
);
```

预填充方式: App 首次启动时从 `assets/medicine_library.json` 读取并批量 INSERT。

### 静态数据文件（JSON）

#### res/raw/elderly_avatars.json — 老人头像索引

```json
{
  "male": [
    "avatars/elderly/male_1.jpg",
    "avatars/elderly/male_2.jpg",
    "...共10个"
  ],
  "female": [
    "avatars/elderly/female_1.jpg",
    "avatars/elderly/female_2.jpg",
    "...共6个"
  ]
}
```

字段说明:
- `male`: `String[]` — 男性老人头像文件名列表（10个）
- `female`: `String[]` — 女性老人头像文件名列表（6个）
- 路径为 `assets/avatars/elderly/` 下的相对路径

#### res/raw/family_avatars.json — 家属头像索引

```json
{
  "childMale": ["avatars/family/child_male_1.svg", "..."],
  "childFemale": ["avatars/family/child_female_1.svg", "..."],
  "middleMale": ["avatars/family/middle_male_1.svg", "..."],
  "middleFemale": ["avatars/family/middle_female_1.svg", "..."]
}
```

字段说明:
- `childMale`: `String[]` — 男性晚辈头像（10个），用于儿子/孙子等
- `childFemale`: `String[]` — 女性晚辈头像（10个），用于女儿/孙女等
- `middleMale`: `String[]` — 男性同辈头像（10个），用于配偶/兄弟等
- `middleFemale`: `String[]` — 女性同辈头像（10个），用于配偶/姐妹等
- 路径为 `assets/avatars/family/` 下的相对路径

#### res/raw/fraud_prevention_tips.json — 防诈骗提示

```json
[
  {
    "id": 1,
    "title": "冒充公检法诈骗",
    "summary": "骗子冒充公安、检察院、法院工作人员...",
    "detail": "骗子会准确说出你的姓名、身份证号...",
    "measures": ["公检法机关不会通过电话办案", "..."],
    "icon": "shield_alert",
    "color": "red",
    "cases": [
      { "title": "案例1", "content": "张阿姨接到公安局电话..." }
    ]
  }
]
```

字段说明:
- `id`: `int` — 提示唯一 ID
- `title`: `String` — 诈骗类型标题
- `summary`: `String` — 一句话摘要
- `detail`: `String` — 详细描述
- `measures`: `String[]` — 防范措施列表（3-4条）
- `icon`: `String` — 图标标识（对应 Android drawable 资源名）
- `color`: `String` — 主题色标识（对应 colors.xml 中的颜色名）
- `cases`: `Object[]` — 真实案例列表，每个含 `title` 和 `content`

#### assets/medicine_library.json — 药品库数据

```json
[
  {
    "disease": "高血压",
    "medicines": [
      {
        "medicine_name": "硝苯地平缓释片",
        "brand": "拜新同",
        "image": "medicines/hypertension/nifedipine.jpg",
        "description": "用于治疗高血压和心绞痛"
      }
    ]
  }
]
```

字段说明:
- `disease`: `String` — 疾病名称（17 种疾病，共约 80 种药品）
- `medicines`: `Object[]` — 该疾病对应的药品列表
  - `medicine_name`: `String` — 药品通用名
  - `brand`: `String` — 商品名
  - `image`: `String` — 药品图片在 assets 内的相对路径
  - `description`: `String` — 药品简要说明

### SharedPreferences 键值

| Key | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| `last_user_id` | int | -1 | 上次登录的用户 ID，用于快速登录 |
| `app_first_launch` | boolean | true | 是否首次启动，用于初始化药品库数据 |
| `font_size` | String | "large" | 字体大小偏好: `"normal"` / `"large"` / `"xlarge"` |
| `reminder_enabled` | boolean | true | 用药提醒总开关 |

### 智谱 API 对接

- URL: `https://open.bigmodel.cn/api/paas/v4/chat/completions`
- Model: `glm-4`
- System Prompt 保持与原 Express 后端一致（含疾病-药品映射表 + 老年人安全劝解规则）
- 请求线程模型: `Thread` → `HttpURLConnection` → `Handler(Looper.getMainLooper())` 更新 UI

### 用药提醒机制

- `AlarmManager.setRepeating()` 按药品服用时间设置闹钟
- `ReminderBroadcastReceiver` 收到广播后弹出 `Notification`
- 点击通知进入 MedicineFragment 完成打卡
- `BootReceiver` 监听 `BOOT_COMPLETED` 恢复所有闹钟

### 架构约束

- 不引入 Room、Retrofit、Coroutines 等 Jetpack 组件（课程不覆盖）
- API Key 存 `res/values/strings.xml`
- 照片存 `context.getFilesDir()/photos/`
- 静态数据（头像、防诈骗提示）存 `res/raw/*.json`，药品库存 `assets/` 首次启动导入

## Testing Decisions

### 测试原则

- 只测试模块的外部行为（输入→输出），不测试内部实现细节
- DAO 测试使用真实的 SQLite 数据库（非 mock）—— SQLiteOpenHelper 是 Android SDK 内嵌，不需要 mock

### 需要测试的模块

| 模块 | 测试内容 | 测试方式 |
|------|---------|---------|
| `db` | 每张表的 CRUD 操作正确性、外键约束、唯一约束（如 medicine_taken 的 UNIQUE(user_id, medicine_id, date)） | Android Instrumentation Test (Robolectric 可选) |
| `network` | System Prompt 拼装正确性、JSON 请求体格式、响应解析逻辑 | JUnit（不实际发网络请求，测试 JSON 构造和解析逻辑） |
| `reminder` | AlarmManager 设置的时间计算逻辑、通知内容拼装 | JUnit |
| `util` | JSON 文件读取、日期格式化、SharedPreferences 读写 | JUnit |

### 不需要测试的模块

- `ui-*` 模块——UI 行为通过手动功能测试验证（课程范围外）
- `voice` 模块——依赖硬件，测试成本高

### 测试文件位置

```
app/src/test/java/.../  — JUnit 测试 (network, reminder, util)
app/src/androidTest/java/.../  — Instrumentation 测试 (db)
```

## Out of Scope

- 社区搜索（周边服务地图）—— 原 Vue 项目中引用 mapService.js，移植到 Android 需 Google Maps 或高德地图 SDK，超出课程范围
- 社区搜索功能 —— 同上
- 用户数据云同步 —— 纯本地 App，不涉及
- 多语言支持 —— 仅中文
- 平板适配 —— 仅手机竖屏
- CI/CD 自动化测试 —— 手动运行测试即可

## Further Notes

- 原 Vue App.vue 约 5221 行，移植后预计分散到 ~20 个文件（每个 Activity/Fragment 单独文件 + XML 布局文件）
- 开发工具: Android Studio, minSdk 26 (Android 8.0), targetSdk 34
- 语言: Java（课程教材语言，若课程允许 Kotlin 可切换）
- 测试用虚拟设备: Pixel 6 API 34 (Android 14)
- 蓝牙功能需真机测试，模拟器不支持
