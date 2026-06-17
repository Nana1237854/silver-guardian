# 银发守护者 —— 课程新需求设计方案

> 对应课程：广州商学院《移动软件开发》2025-2026(2) 选题二：老年人智慧生活助手 APP
> 评分依据：(移动软件开发）评分标准、评分表.xls

---

## 一、需求对照与评分覆盖

| # | 功能要求 | 技术考察点 | 评分 | 状态 |
|---|---------|-----------|------|------|
| 1 | 适老化界面 | SharedPreferences | APP 实现 | 已设计 |
| 2 | 一键呼叫 | 权限管理（CALL_PHONE） | APP 实现 | 已设计 |
| 3 | 定时用药提醒 | Notification + TTS | APP 实现 | 已有基础 |
| 4 | 社区便民查询 | 高德地图 SDK + 权限管理（定位） | APP 实现 | 新增 |
| 5 | 防诈骗知识推送 | OkHttp + RecyclerView | APP 实现 | 新增 |
| — | 整体数据持久化 | SQLite（11 张表） | APP 实现 | 已有 |
| — | 项目说明书 | — | 项目说明书（20分） | 待撰写 |
| — | 答辩演示 | — | 答辩演示（20分） | 待准备 |

## 二、功能一：适老化界面

### 2.1 设计目标

- 大字体模式可切换（normal / large / xlarge 三档）
- 高对比度配色（深色文字 + 浅色背景，避免低对比度灰色）
- 按钮高度不低于 56dp，文字不小于 18sp

### 2.2 实现方案

```
SettingsFragment
    └── 大字模式 Spinner: 标准 / 加大 / 特大
        └── SharedPreferences.putString("font_mode", selected)
            └── 重启当前 Activity（或动态刷新）
```

### 2.3 文件清单

| 文件 | 改动 |
|------|------|
| `SettingsFragment.java` | 加字号选择 Spinner |
| `utils/FontScaleHelper.java` | 新建：提供 `sp(int baseDp)` 方法根据 sharedPref 缩放 |
| `res/values/dimens.xml` | 已有基准尺寸，无需改 |

### 2.4 SharedPreferences 键值

| Key | 类型 | 默认 | 说明 |
|-----|------|------|------|
| `font_mode` | String | `"large"` | 字号：normal / large / xlarge |
| `theme_high_contrast` | boolean | `true` | 高对比度模式 |

## 三、功能二：一键呼叫

### 3.1 设计目标

- 主界面常驻"一键呼叫"按钮，点击直接拨打家属电话
- 提供 120 急救入口（长按或下拉选择）
- 覆盖"权限管理"技术考察点

### 3.2 实现方案

```
MainActivity / HealthFragment
    └── 一键呼叫按钮（红色醒目，56dp 高）
        ├── 单击 → 拨打第一位在线家属
        └── 长按 → 弹出选项：儿子 / 女儿 / 120
```

### 3.3 权限流程

```
AndroidManifest: <uses-permission android:name="android.permission.CALL_PHONE" />

运行时:
if (ContextCompat.checkSelfPermission() != PERMISSION_GRANTED)
    → ActivityCompat.requestPermissions(CALL_PHONE_REQUEST_CODE)
    → onRequestPermissionsResult()
        → granted: Intent(ACTION_CALL, Uri.parse("tel:13800001111"))
        → denied: Toast + 弹窗引导去设置
```

### 3.4 文件清单

| 文件 | 改动 |
|------|------|
| `AndroidManifest.xml` | 加 `<uses-permission CALL_PHONE>` |
| `MainActivity.java` | 加一键呼叫按钮 + 权限申请逻辑 |
| `HealthFragment.java` | 已有 SOS 按钮，改为调用真实拨号 |

## 四、功能三：定时用药提醒（增强）

### 4.1 设计目标

- 通知栏弹出用药提醒（已设计）
- **新增：TTS 语音播报提醒内容**
- 课程考察：BroadcastReceiver + Notification + TTS

### 4.2 实现方案

```
AlarmManager.setExact() → PendingIntent
    └── ReminderBroadcastReceiver.onReceive()
        ├── NotificationManager.notify()  → 通知栏
        └── TextToSpeech.speak()          → 语音播报
```

### 4.3 TTS 初始化

```java
TextToSpeech tts = new TextToSpeech(context, status -> {
    if (status == TextToSpeech.SUCCESS) {
        tts.setLanguage(Locale.CHINESE);
        tts.setSpeechRate(0.8f);  // 稍慢，适合老人
    }
});
// 播报: tts.speak("颜爷爷，该吃硝苯地平缓释片了", TextToSpeech.QUEUE_FLUSH, null, "med_1");
```

### 4.4 文件清单

| 文件 | 改动 |
|------|------|
| `ReminderBroadcastReceiver.java` | 加 TTS 初始化 + speak 调用 |
| `MedicineFragment.java` | "添加药品"表单改为真实（时间选择器 + 剂量输入） |

## 五、功能四：社区便民查询（新增）

### 5.1 设计目标

- 集成高德 3D 地图 SDK
- 支持搜索"菜市场"、"社区卫生服务中心"、"药店"
- 提供步行/驾车路径导航
- 覆盖"地图 SDK"和"定位权限管理"考察点

### 5.2 实现方案

```
BottomNavigation: 新增 tab "便民查询"
    └── CommunityFragment
        ├── MapView（高德 3D 地图）
        │   ├── 显示当前定位蓝点
        │   └── 搜索按钮：菜市场 | 社区医院 | 药店
        ├── PoiSearch.Query(关键词, "", 当前经纬度)
        │   └── PoiSearch.SearchBound(当前点, 3000米)
        │       └── onPoiSearched() → 在地图上标 Marker
        └── 点击 Marker → 底部弹出 POI 详情
            └── "一键导航" → RouteSearch.calculateWalkRoute()
                └── 跳转高德地图 App 或 Web
```

### 5.3 权限清单

| 权限 | 用途 |
|------|------|
| `ACCESS_FINE_LOCATION` | 获取当前位置 |
| `ACCESS_COARSE_LOCATION` | 粗略位置（备用） |
| `ACCESS_NETWORK_STATE` | 高德 SDK 网络检测 |
| `READ_PHONE_STATE` | 高德 SDK 设备标识 |
| `WRITE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` | 高德离线地图缓存 |

### 5.4 文件清单

| 文件 | 改动 |
|------|------|
| `app/build.gradle` | 加高德 3D 地图 + 搜索 SDK 依赖 |
| `AndroidManifest.xml` | 加定位权限 + 高德 API Key `<meta-data>` |
| `CommunityFragment.java` | 新建：MapView + PoiSearch + RouteSearch |
| `fragment_community.xml` | 新建：MapView 占满 + 顶部搜索栏 |
| `bottom_nav_menu.xml` | 加便民查询 tab |

## 六、功能五：防诈骗知识推送（新增）

### 6.1 设计目标

- 每日推送一条防电信诈骗知识
- 点击可查看详细案例 + 防范措施
- 用 OkHttp 从远程 API 拉取（技术考察点）

### 6.2 实现方案

```
FraudFragment
    ├── 每日一推卡片（大标题 + 摘要）
    │   └── 点击 → FraudDetailActivity（完整内容 + 案例）
    ├── RecyclerView 历史推送列表
    └── 拉取逻辑（OkHttp）
        ├── OkHttpClient.GET(API_URL + "?key=xxx&type=反诈骗")
        ├── 解析 JSON → 存入 SQLite fraud_tips 表
        └── 网络异常 → 用本地 assets/fraud_tips.json 兜底
```

### 6.3 API 对接设计

```java
// OkHttp 异步 GET
OkHttpClient client = new OkHttpClient();
Request request = new Request.Builder()
    .url("https://apis.juhe.cn/fraud/news?key=YOUR_API_KEY&type=fraud")
    .build();
client.newCall(request).enqueue(new Callback() {
    @Override public void onResponse(Call call, Response response) {
        String json = response.body().string();
        // Handler → 主线程 → 解析 JSON → 更新 SQLite + RecyclerView
    }
    @Override public void onFailure(Call call, IOException e) {
        loadLocalFallback();  // 从 assets 加载兜底数据
    }
});
```

### 6.4 文件清单

| 文件 | 改动 |
|------|------|
| `app/build.gradle` | 加 `implementation "com.squareup.okhttp3:okhttp:4.12.0"` |
| `FraudFragment.java` | 改为 OkHttp 拉取 + RecyclerView 展示列表 |
| `FraudDetailActivity.java` | 新建：详情 + 案例 + 防范措施 |
| `FraudApiClient.java` | 新建：OkHttp 封装 |
| `assets/fraud_tips.json` | 新建：本地兜底数据（10 条） |

## 七、技术考察点覆盖汇总

| 考察点 | 覆盖位置 | 代码路径 |
|--------|---------|---------|
| SharedPreferences | 适老化主题切换 | `FontScaleHelper.java` → `getSharedPreferences()` |
| SQLite | 11 张表持久化（所有模块） | `ElderlyDbHelper.java` |
| RecyclerView | 健康列表/用药列表/相册网格/防诈骗列表/社区 POI 列表 | 5 处使用 |
| 权限管理 | CALL_PHONE + FINE_LOCATION + POST_NOTIFICATIONS | `MainActivity` / `CommunityFragment` |
| 地图 SDK | 高德 MapView + PoiSearch + RouteSearch | `CommunityFragment.java` |
| 第三方网络请求库 | OkHttp GET 防诈骗 API | `FraudApiClient.java` |

## 八、评分对照

| 评分项 | 分值 | 覆盖情况 |
|--------|------|---------|
| APP 内容健康、布局合理、结构连贯 | 5分 | 健康 + 适老化 + 大按钮布局 ✓ |
| 整体风格一致、色彩合理、美观 | 5分 | 统一 mint 色调 + 高对比度 ✓ |
| 运行稳定、流畅、具备实用性 | 5分 | 离线和在线双模式 ✓ |
| 功能完备、知识点覆盖面广 | 45分 | 5 项功能 + 6 项技术点全覆盖 ✓ |
| 项目说明书 | 20分 | 已有 PRD + 本设计文档 ✓ |
| 答辩演示 | 20分 | 待准备 |
| **合计** | **100分** | |

## 九、依赖清单

```groovy
// app/build.gradle
dependencies {
    // 现有
    implementation "androidx.appcompat:appcompat:1.6.1"
    implementation "androidx.recyclerview:recyclerview:1.3.2"
    implementation "com.github.bumptech.glide:glide:4.16.0"
    implementation "com.google.android.material:material:1.11.0"

    // 新增
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
    implementation "com.amap.api:3dmap:10.0.600"
    implementation "com.amap.api:search:9.7.0"
}
```

## 十、AndroidManifest 新增权限

```xml
<!-- 一键呼叫 -->
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- 社区便民查询 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 用药提醒通知（Android 13+） -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 高德地图 Key -->
<meta-data android:name="com.amap.api.v2.apikey" android:value="YOUR_AMAP_KEY" />
```
