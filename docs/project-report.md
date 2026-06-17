# 项目说明书 — 银发守护者（老年人智慧生活助手APP）

> **项目名称**：银发守护者（Silver Guardian）  
> **考查学期**：2025-2026学年 第2学期  
> **考查方式**：课程设计  
> **选题**：选题二：老年人智慧生活助手APP  

---

## 1 项目概况

### 1.1 项目简介

"银发守护者"是一款面向老年人群体的智慧生活助手 Android 应用。针对老年人数字使用门槛高的痛点，系统提供大字体/高对比度界面、AI 智能对话、定时用药管理与语音播报提醒、一键紧急呼叫、社区便民地图查询及防诈骗知识每日推送等功能，助力老年人安全、便捷地融入智能生活。

### 1.2 项目功能图

```
银发守护者 APP
├── 1. 适老化界面
│   ├── 大字体模式切换（标准/加大/特大）
│   └── 高对比度主题切换
├── 2. 用户管理
│   ├── PIN 码登录
│   ├── 多用户切换
│   └── 用户增删
├── 3. 首页中枢（HomeFragment）
│   ├── 日期天气展示
│   ├── 健康快报摘要
│   ├── 家属在线列表
│   └── AI 对话入口
├── 4. 健康管理
│   ├── 健康档案CRUD（血压/血糖/心率/步数/血氧/体温/体重/体脂率/呼吸率/睡眠/心情/运动/呼吸正念）
│   ├── 蓝牙设备数据导入（血压计/血氧仪）
│   └── AI 健康分析
├── 5. AI 智能对话（ChatDetailActivity）
│   ├── 文字/语音双输入
│   ├── 智谱 GLM-4 API 驱动回复
│   ├── 疾病药品智能推荐
│   └── 老年人安全劝解规则
├── 6. 用药提醒（MedicineFragment）
│   ├── 药品CRUD管理
│   ├── 定时闹钟提醒（AlarmManager）
│   ├── 通知栏推送 + TTS语音播报
│   ├── 服药打卡
│   └── 药品库搜索
├── 7. 一键呼叫
│   ├── 一键拨打首位在线家属
│   ├── 长按选择呼叫对象（家属/120）
│   └── CALL_PHONE 运行时权限管理
├── 8. 亲情相册（AlbumFragment）
│   ├── 相册列表→照片网格两层结构
│   ├── 系统相册选择 + 权限管理
│   ├── 照片详情预览（Glide加载）
│   └── 收藏/删除管理
├── 9. 子女监控面板（ChildModeActivity）
│   ├── 老人健康数据汇总
│   ├── 服药打卡状态查看
│   └── 家属上传照片
├── 10. 社区便民查询（CommunityActivity）
│   ├── 高德3D地图SDK集成
│   ├── POI兴趣点搜索（菜市场/医院/药店/超市/银行）
│   ├── 步行路径规划
│   └── 一键导航（高德地图App跳转）
├── 11. 防诈骗知识推送（FraudFragment）
│   ├── OkHttp拉取远程防诈骗数据
│   ├── 每日推送卡片展示
│   ├── 详细案例+防范措施查看
│   └── 本地数据兜底
├── 12. 记忆回忆（MemoryFragment）
│   ├── 生活记事CRUD
│   └── 分类筛选（健康/爱好/家庭/其他）
└── 13. 设置（SettingsFragment）
    ├── 大字体/高对比度切换
    ├── 功能入口汇总
    └── 退出登录
```

---

## 2 系统功能

### 2.1 适老化界面

大字体模式通过 SharedPreferences 存储用户偏好（normal/large/xlarge），所有页面通过 BaseFragment 继承的 `sp()` 方法动态读取缩放比例（1.0x/1.2x/1.4x）应用到文本。高对比度模式切换后调用 `requireActivity().recreate()` 全局刷新，将文字颜色切换为纯黑 #000000、背景切换为纯白 #FFFFFF。

#### 业务流程：
1. 用户进入"设置"页面
2. 点击"大字模式"下拉选择：标准 / 加大 / 特大
3. Spinner 触发 `FontScaleHelper.setFontMode()` 写入 SharedPreferences
4. `requireActivity().recreate()` 重建所有页面
5. 各 Fragment 的 `sp()` 方法读取缩放比并应用到 `setTextSize(sp(baseSize))`

### 2.2 一键呼叫

主界面常驻"一键呼叫"按钮。单击直接拨打电话给第一位在线家属；长按弹出选项列表（家属成员 + 120 急救中心）。

#### 业务流程：
1. 用户在首页/健康页面看到红色"📞 一键呼叫"按钮
2. 单击 → 检查 CALL_PHONE 权限 → 未授权则弹出系统权限弹窗
3. 权限通过 → `Intent(ACTION_CALL, Uri.parse("tel:138xxxxxxxx"))` 直接拨出
4. 长按 → 弹出 AlertDialog 选项列表 → 选择后拨号

### 2.3 定时用药提醒

用户可添加药品信息（名称、类型、服用时间、剂量、用法说明），系统通过 AlarmManager 设置定时闹钟。触发时，BroadcastReceiver 发送通知栏推送并调用系统 TTS 引擎语音播报提醒内容。

#### 业务流程：
1. 用户进入"用药提醒"Tab，点击 + 添加药品
2. 填写药品信息（名称、类型、服用时间如"08:00,20:00"、用法、说明）
3. 支持从药品库搜索自动填充（搜索"高血压"→匹配硝苯地平/缬沙坦等）
4. 闹钟触发 → ReminderBroadcastReceiver.onReceive()
5. → NotificationManager 发送通知栏
6. → TextToSpeech.speak("颜爷爷，该吃硝苯地平缓释片了") 语音播报
7. 用户点击打卡 CheckBox 确认已服

### 2.4 社区便民查询

集成高德 3D 地图 SDK，在地图上展示当前位置，支持 POI 关键词搜索（菜市场、社区卫生服务中心、药店、超市、银行），搜索结果以 5 色 Marker 标注在地图上。点击 Marker 显示详情（地址、距离、电话），支持步行路径规划与跳转高德 App 一键导航。

#### 业务流程：
1. 用户从设置页进入"便民查询"
2. 高德 SDK 初始化隐私合规后加载 MapView
3. 用户点击搜索按钮（如"🥬 菜市场"）
4. PoiSearch.Query + SearchBound(5000m) → 异步搜索
5. onPoiSearched() → 在地图上标注 5 色 Marker
6. 点击 Marker → 弹窗显示地址/距离/电话
7. 点击"步行导航" → RouteSearch.calculateWalkRoute() → 显示距离/时间
8. 点击"打开高德导航" → Intent 调起高德 App 或浏览器

### 2.5 防诈骗知识推送

使用 OkHttp 第三方网络库从远程 API 拉取反电信诈骗知识数据，解析 JSON 后在 RecyclerView 列表中展示。点击条目可查看详细案例和防范措施。网络异常时自动回退到本地 assets 兜底数据。

#### 业务流程：
1. 用户从设置页进入"防诈提醒"
2. FraudFragment 初始化时调用 FraudApiClient.fetchFraudTips()
3. OkHttp GET 请求远程 API → 解析 JSONArray → 存入本地列表
4. RecyclerView 展示标题卡片（标题 + 分类 + 简述）
5. 点击 → FraudDetailActivity 展示完整案例 + 防范措施
6. 网络异常 → 加载 assets/fraud_tips.json 本地兜底数据

---

## 3 页面设计与实现

### 3.1 页面总览

本系统包含 **13 个 Activity** 和 **9 个 Fragment**，覆盖所有功能模块。

| 页面 | 类型 | 说明 |
|------|------|------|
| LoginActivity | Activity | PIN码登录，用户网格选择 |
| MainActivity | Activity | 底部导航容器（5个Tab） |
| ChatDetailActivity | Activity | AI全屏对话 |
| PhotoDetailActivity | Activity | 照片详情大图 |
| ChildModeActivity | Activity | 子女监控面板 |
| CommunityActivity | Activity | 高德地图便民查询 |
| BluetoothActivity | Activity | 蓝牙设备扫描管理 |
| FraudDetailActivity | Activity | 防诈骗详情页 |
| HomeFragment | Fragment | 首页中枢 |
| HealthFragment | Fragment | 健康探索 |
| MedicineFragment | Fragment | 用药提醒 |
| AlbumFragment | Fragment | 亲情相册 |
| SettingsFragment | Fragment | 设置 |
| MemoryFragment | Fragment | 记忆回忆 |
| FraudFragment | Fragment | 防诈列表 |

### 3.2 界面布局设计

**LoginActivity** — 使用 LinearLayout 垂直布局，包含：
- 品牌区域（图标 + 标题文字）
- RecyclerView（GridLayoutManager 3列）展示用户头像网格
- CardView 包裹的 PIN 输入区域
- Button 登录按钮

**MainActivity** — 使用 LinearLayout + FrameLayout + BottomNavigationView：
- FrameLayout 作为 Fragment 容器
- BottomNavigationView 5个Tab（健康探索/用药提醒/首页中枢/亲情相册/设置）

**HealthFragment** — 使用 ScrollView + LinearLayout 垂直布局：
- 每个健康指标为水平 LinearLayout（图标 + 指标名 + 数值 + 状态标签）

**AlbumFragment** — 两种视图状态：
- 相册列表：RecyclerView + StaggeredGridLayoutManager(2列)
- 照片网格：RecyclerView + StaggeredGridLayoutManager(2列) + 返回按钮

**CommunityActivity** — 使用 LinearLayout 垂直布局：
- HorizontalScrollView 搜索栏（5个快捷按钮）
- MapView（高德3D地图）
- 底部返回按钮

### 3.3 主要控件与布局使用

| 控件/布局 | 应用位置 | 解决的问题 |
|----------|---------|-----------|
| RecyclerView + GridLayoutManager | 相册网格、用户选择 | 大列表高性能显示，避免OOM |
| RecyclerView + LinearLayoutManager | 健康列表、药品列表、防诈列表 | 大数据量滚动展示 |
| CardView | 健康卡片、相册卡片、设置卡片 | 提升卡片视觉效果 |
| BottomNavigationView | MainActivity | 5个核心页面快速切换 |
| ScrollView | HealthFragment、ChildModeActivity | 内容超出屏幕时滚动 |
| HorizontalScrollView | 健康操作栏、便民搜索栏 | 横向快捷操作 |
| AlertDialog | SOS、添加药品、上传照片、删除确认 | 模态操作确认 |
| MapView (高德) | CommunityActivity | 地图展示与交互 |
| Spinner | 药品筛选、相册分类、字号选择 | 下拉选项筛选 |
| StaggeredGridLayoutManager | 相册网格 | 瀑布流交错布局 |

---

## 4 系统功能具体实现

### 4.1 数据存储（SQLite）

使用 `ElderlyDbHelper extends SQLiteOpenHelper` 管理 11 张数据表，所有数据按 `user_id` 隔离。关键技术：手写 SQL 建表、ContentValues 插入、Cursor 查询、事务性删除。

### 4.2 适老化界面

- `FontScaleHelper` 工具类封装 SharedPreferences 读写
- `BaseFragment.sp()` 方法动态缩放：`Math.round(baseSp * getTextScale(context))`
- `FontScaleHelper.textPrimary()` / `FontScaleHelper.bgPage()` 高对比度颜色切换

### 4.3 一键呼叫

- `MainActivity.oneTapCall()` — 遍历 MockData 找到首位在线家属
- `ContextCompat.checkSelfPermission(CALL_PHONE)` → `ActivityCompat.requestPermissions()`
- `Intent(ACTION_CALL, Uri.parse("tel:" + phone))`

### 4.4 定时用药提醒

- `ReminderBroadcastReceiver` — 接收 AlarmManager 广播
- `NotificationCompat.Builder` — 构建通知
- `TextToSpeech.speak(message, QUEUE_FLUSH, null, "med_reminder")` — 语音播报
- `BootReceiver` — 开机自启恢复所有闹钟

### 4.5 社区便民查询（高德 SDK）

- `MapsInitializer.updatePrivacyShow/updatePrivacyAgree` — 隐私合规（必须在所有 API 前调用）
- `MapView` 3D 地图 + `MyLocationStyle` 定位蓝点
- `PoiSearch.Query + SearchBound(5000m)` — 周边 POI 搜索
- `RouteSearch.WalkRouteQuery` — 步行路径规划
- Intent 调起高德 App：`androidamap://navi?sourceApplication=...&lat=...&lon=...`

### 4.6 防诈骗推送（OkHttp）

- `OkHttpClient.Builder().connectTimeout(10, SECONDS)` — 超时配置
- `client.newCall(request).enqueue(callback)` — 异步 GET
- `JSONObject.getJSONArray("data")` — JSON 解析
- 网络失败 → `assets/fraud_tips.json` 本地兜底

### 4.7 语音输入

- `SpeechRecognizer.createSpeechRecognizer(this)` — 系统语音识别
- `RecognitionListener` — 8 种错误码中文提示
- `RECORD_AUDIO` 运行时权限

### 4.8 蓝牙设备

- `BluetoothLeScanner.startScan(scanCallback)` — BLE 扫描
- `BLUETOOTH_SCAN + BLUETOOTH_CONNECT` 权限
- 模拟器/无蓝牙 → 自动降级为模拟设备

### 4.9 亲情相册

- `Intent(ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)` — 系统图库
- `READ_MEDIA_IMAGES`（Android 13+）/ `READ_EXTERNAL_STORAGE`（旧版）权限
- `Glide.with(context).load(Uri.parse(photo.url)).centerCrop().into(imageView)` — 图片加载
- `GalleryPermissionHelper` 统一权限处理

---

## 5 系统功能测试

| 编号 | 测试功能 | 测试步骤 | 预期结果 | 状态 |
|------|---------|---------|---------|------|
| T01 | PIN登录 | 选择用户→输入正确PIN→登录 | 进入主页 | ✅ |
| T02 | PIN错误 | 输入错误PIN→登录 | Toast提示错误 | ✅ |
| T03 | 多用户切换 | 登录爷爷→退出→登录奶奶 | 数据切换成功 | ✅ |
| T04 | 添加用户 | 点击添加→填写→保存 | 新用户出现在列表 | ✅ |
| T05 | 大字模式 | 设置→选"特大"→查看页面 | 字号明显放大 | ✅ |
| T06 | 高对比度 | 设置→开关高对比度→查看 | 文字变黑背景变白 | ✅ |
| T07 | 一键呼叫 | 健康页→点一键呼叫→授权 | 弹出系统拨号 | ✅ |
| T08 | 长按选人呼叫 | 长按一键呼叫→选120 | 拨打120 | ✅ |
| T09 | AI对话 | 输入"高血压怎么办"→发送 | AI回复含药品推荐 | ✅ |
| T10 | AI安全劝解 | 输入"我要跑10公里" | AI温和劝阻 | ✅ |
| T11 | 语音输入 | 点麦克风→说话 | 文字填入输入框 | ✅ |
| T12 | 添加药品 | 用药→点+→填写→保存 | 药品出现在列表 | ✅ |
| T13 | 药品库搜索 | 添加药品→搜索"高血压" | 自动填充药品信息 | ✅ |
| T14 | 服药打卡 | 点未打卡药品CheckBox | 变为已打卡，Toast提示 | ✅ |
| T15 | 药品筛选 | Spinner切换类型 | 列表过滤显示 | ✅ |
| T16 | 删除药品 | 长按药品→确认删除 | 药品消失 | ✅ |
| T17 | 相册列表 | 进入亲情相册Tab | 显示相册分类卡片 | ✅ |
| T18 | 创建相册 | 点创建→输入名称→确定 | 进入新相册 | ✅ |
| T19 | 进入相册 | 点某个相册卡片 | 显示该相册照片 | ✅ |
| T20 | 上传照片 | 相册内→上传→选图→保存 | 照片出现在网格 | ✅ |
| T21 | 照片详情 | 点击照片 | PhotoDetailActivity显示大图 | ✅ |
| T22 | 收藏照片 | 长按照片 | 收藏状态切换 | ✅ |
| T23 | 删除相册 | 长按相册卡片→确认 | 相册及照片删除 | ✅ |
| T24 | 便民查询 | 设置→便民查询→搜索 | 地图显示Marker | ✅ |
| T25 | 步行导航 | 点Marker→步行导航 | 显示路线信息 | ✅ |
| T26 | 防诈推送 | 设置→防诈提醒 | 显示防诈列表 | ✅ |
| T27 | 防诈详情 | 点防诈条目 | FraudDetailActivity显示 | ✅ |
| T28 | 子女模式 | 设置→子女模式 | 显示健康/用药汇总 | ✅ |
| T29 | 蓝牙扫描 | 设置→蓝牙→扫描 | 显示设备列表 | ✅ |
| T30 | 蓝牙模拟 | 点加载模拟设备 | 显示3个模拟设备 | ✅ |
| T31 | 记忆管理 | 设置→记忆回忆 | 显示记忆列表 | ✅ |
| T32 | 新增记忆 | 点新增→填写→保存 | 新记忆出现 | ✅ |
| T33 | 删除记忆 | 长按记忆→确认 | 记忆消失 | ✅ |
| T34 | 数据持久化 | 添加数据→退出重登 | 数据仍在 | ✅ |
| T35 | 24单元测试 | `./gradlew test` | BUILD SUCCESSFUL | ✅ |

---

## 6 系统实现所使用的相关AI工具

本项目的开发过程中使用了 Claude Code（Anthropic 的 AI 编程助手）辅助完成以下工作：

1. **需求分析与架构设计**：通过 `/grill-with-docs` 命令系统化梳理课程需求，将课程文档要求转化为具体技术方案。AI 协助完成 5 项功能需求 ↔ 6 项技术考察点的映射。

2. **原型快速搭建**：通过 `/prototype` 命令基于 Vue 原项目快速构建 Android 原型，建立了 Activity/Fragment 骨架、MockData 内存数据层、XML 布局资源。

3. **测试驱动开发**：通过 `/tdd` 命令编写 4 个模块共 24 个单元测试（Robolectric + JUnit + Instrumentation），确保核心逻辑正确性。

4. **Bug 诊断与修复**：通过 `/diagnose` 命令系统化排查运行期崩溃（BottomNavigationView 超 5 项限制、高德 SDK 隐私合规校验失败、SimpleDateFormat 非法字符、相册权限缺失等）。

5. **架构重构**：通过 `/improve-codebase-architecture` 命令提取 BaseFragment 消除 9 个 Fragment 的重复代码，拆分 MockData 上帝对象，提炼独立可测试组件。

6. **问题拆解**：通过 `/to-issues` 命令将剩余工作拆分为 7 个独立可抓取的 GitHub Issues，按依赖关系排序。

7. **项目说明书撰写**：本文档由 AI 辅助按照课程模板格式整理生成，涵盖项目概况、系统功能、页面设计、技术实现、测试用例等章节。

---

## 7 小组成员分工以及收获体会

### 7.1 小组成员分工

| 成员 | 学号 | 分工 | 占比 |
|------|------|------|------|
| （填写） | （填写） | 需求分析、系统架构设计、核心模块开发、测试 | xx% |
| （填写） | （填写） | UI设计、页面实现、数据层开发 | xx% |
| （填写） | （填写） | 文档撰写、测试用例设计、答辩准备 | xx% |

### 7.2 收获与体会

（由小组成员分别填写个人体会）

> 示例：
> 通过本次课程设计，我对 Android 开发有了系统的认识。从最初的界面布局设计，到 Activity/Fragment 生命周期管理，再到 SQLite 数据持久化和第三方 SDK 集成，完整经历了一款 App 从零到一的全过程。特别是高德地图 SDK 集成过程中遇到的隐私合规问题和架构兼容性挑战，让我深刻理解了查阅官方文档和错误日志的重要性。
>
> 课程所学的 LinearLayout、RecyclerView、SharedPreferences、SQLiteOpenHelper、BroadcastReceiver 等知识点在实际项目中得到了充分运用，理论联系实践加深了理解。同时，AI 辅助编程工具的引入大大提高了开发效率，但也让我意识到理解底层原理的重要性——AI 可以帮你写代码，但无法替代你对系统架构的判断和对用户体验的思考。
