# 银发守护者 Android 移植设计方案

## 1. 技术栈

| 层 | 选型 | 课程章节 |
|---|---|---|
| UI | XML + ViewBinding | 第 2-3 章 |
| 导航 | Activity + Fragment + Intent | 第 4 章 |
| 存储 | SQLiteOpenHelper + 手写 SQL | 第 5 章 |
| 网络 | HttpURLConnection + JSONObject | 第 9.1-9.3 节 |
| 异步 | Handler + Thread | 第 9.4 节 |
| 后台 | Service | 第 8 章 |
| 广播 | BroadcastReceiver | 第 7 章 |
| 图片加载 | Glide | 第三方库 |
| 语音输入 | Android SpeechRecognizer | SDK 内置 |
| API Key | strings.xml | — |

## 2. Activity/Fragment 架构

```
LoginActivity                          ← PIN 登录 + 多用户选择
    │
    └── Intent ──▶ MainActivity        ← DrawerLayout 容器
                       │
                       ├── HomeFragment          ← 首页中枢
                       ├── HealthFragment        ← 健康档案
                       ├── MedicineFragment      ← 用药提醒
                       └── AlbumFragment         ← 亲情相册

ChildModeActivity                       ← 子女监控面板
ChatDetailActivity                      ← AI 全屏对话
PhotoDetailActivity                     ← 大图查看
BluetoothActivity                       ← 蓝牙设备管理
```

**弹窗 (AlertDialog / DialogFragment)：**
- SOS 紧急呼叫弹窗
- 添加家属弹窗
- 添加药品弹窗
- 上传照片弹窗
- 用药提醒通知弹窗

## 3. SQLite 数据模型（11 张表）

### 从 MySQL 迁移（10 张）

| 表名 | 说明 | 主键 |
|------|------|------|
| `users` | 老人用户（id, name, pin, avatar, age, health_conditions） | id |
| `messages` | AI 对话历史（id, user_id, content, type, created_at） | id |
| `health_data` | 健康指标记录（id, user_id, type, value, notes, created_at） | id |
| `memories` | 记忆记录（id, user_id, memory, category, created_at） | id |
| `reminders` | 用药提醒计划（id, user_id, message, reminder_time, triggered, type） | id |
| `family_members` | 家属信息（id, user_id, name, relationship, phone, avatar, status） | id |
| `user_medicines` | 药品信息（id, user_id, name, type, time, method, description, image） | id |
| `medicine_taken` | 服药打卡（id, user_id, medicine_id, taken_date, taken_at） | id |
| `album_photos` | 相册照片（id, user_id, url, title, description, category, favorite, scene_tag） | id |
| `emergency_alerts` | 紧急提醒关键词（id, user_id, keyword, message, created_at） | id |

### 新增（1 张）

| 表名 | 说明 | 预填充 |
|------|------|--------|
| `medicine_library` | 药品库（id, disease, medicine_name, brand, description） | App 首次启动从 assets/medicine_library.json 导入 |

### DB Helper 设计

```
ElderlyDbHelper extends SQLiteOpenHelper
    ├── onCreate()       — 建 11 张表 + 预填充药品库
    ├── onUpgrade()      — 版本迁移
    └── 每个 DAO 类对应一张表的 CRUD 方法
```

## 4. 静态数据文件

| 数据 | 位置 | 格式 |
|------|------|------|
| 老人头像列表 | `res/raw/elderly_avatars.json` | JSON 数组 |
| 家属头像列表 | `res/raw/family_avatars.json` | JSON 数组 |
| 防诈骗提示 | `res/raw/fraud_prevention_tips.json` | JSON 数组 |
| 药品库 | `assets/medicine_library.json` | JSON，首次启动导入 SQLite |

## 5. 网络层

### 智谱 GLM-4 API 调用

```
static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
static final String API_KEY = "@string/zhipu_api_key"

请求体: { model: "glm-4", messages: [...], temperature: 0.7, max_tokens: 500 }
响应:  { choices[0].message.content }
```

### 线程模型

```
子线程 (Thread) → HttpURLConnection 请求智谱 API
    ↓
Handler (主线程) → 解析 JSON → 更新 RecyclerView (对话列表)
```

## 6. 后台组件

### 用药提醒

```
MedicineReminderService extends Service
    └── AlarmManager.setRepeating() → PendingIntent
        └── ReminderBroadcastReceiver extends BroadcastReceiver
            └── NotificationManager → 弹出通知
```

### 开机自启

```
BootReceiver extends BroadcastReceiver
    └── 监听 BOOT_COMPLETED → 重启 MedicineReminderService
```

## 7. 特殊功能实现

### 蓝牙 (BluetoothGatt)

```
BluetoothActivity
    ├── BluetoothAdapter → 扫描 BLE 设备
    ├── BluetoothGatt → 连接 + 读取血压/心率数据
    └── 写入 health_data 表
```

### 语音输入 (SpeechRecognizer)

```
SpeechRecognizer.createSpeechRecognizer(this)
    └── RecognitionListener.onResults()
        └── 转为文本填入聊天输入框
```

### 照片处理

```
相册选择: Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
拍照:     Intent(MediaStore.ACTION_IMAGE_CAPTURE)
存储:     context.getFilesDir() + "/photos/" + filename
```

## 8. 课程章节覆盖映射

| 章节 | 落点 |
|------|------|
| 1.1-1.6 Android 基础 | 项目创建、AndroidManifest 配置、res 资源管理 |
| 2.1-2.7 界面布局 | 所有 Fragment/Activity 的 XML 布局（LinearLayout, RelativeLayout, FrameLayout） |
| 3.1-3.3 界面控件 | RecyclerView（列表）、Button、EditText、ImageView、自定义控件 |
| 4.1-4.6 Activity | LoginActivity → MainActivity 跳转、Fragment 管理、Intent 传参 |
| 5.1-5.4 数据存储 | SQLiteOpenHelper、SharedPreferences（用户设置） |
| 7.1-7.3 广播 | ReminderBroadcastReceiver、BootReceiver |
| 8.1-8.5 服务 | MedicineReminderService |
| 9.1-9.4 网络编程 | HttpURLConnection 调用智谱 API、JSONObject 解析、Handler 线程通信 |

## 9. 开发顺序建议

```
Phase 1: LoginActivity + 用户管理（第 4-5 章）
Phase 2: MainActivity + 4 个 Fragment 骨架（第 2-4 章）
Phase 3: HomeFragment 首页中枢（含迷你聊天 + 健康快报）
Phase 4: HealthFragment 健康档案 CRUD
Phase 5: MedicineFragment 用药提醒 + 打卡（第 7-8 章）
Phase 6: AlbumFragment 亲情相册 + 照片上传
Phase 7: ChatDetailActivity AI 全屏对话（第 9 章）
Phase 8: ChildModeActivity 子女监控
Phase 9: BluetoothActivity 蓝牙（创新点）
Phase 10: 全局打磨（通知、弹窗、UI 适配）
```
