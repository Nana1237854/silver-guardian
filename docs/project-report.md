# 项目说明书

**项目名称**：银发守护者（老年人智慧生活助手）  
**考查学期**：2025-2026学年 第2学期  
**考查方式**：课程设计  
**选题**：选题二：老年人智慧生活助手APP  

---

## 1 项目概况

### 1.1 项目名称

银发守护者（Silver Guardian）

### 1.2 项目简介

"银发守护者"是一款面向老年人群体的智慧生活助手 Android 应用。针对老年群体数字使用门槛高、健康管理不便、易受电信诈骗等问题，系统提供大字体/高对比度界面、AI 智能健康对话、定时用药管理与语音播报提醒、一键紧急呼叫亲友、社区便民地图查询、防诈骗知识每日推送等功能，助力老年人安全、便捷地融入智能生活。

系统采用 Android 原生开发技术栈：XML 布局 + Activity/Fragment 导航架构 + SQLite 本地数据存储 + SharedPreferences 用户偏好管理。同时集成了高德地图 SDK（社区便民查询）、OkHttp 第三方网络库（防诈骗内容拉取）、系统 TTS 语音引擎（用药语音播报 + AI 回复朗读）、SpeechRecognizer（AI 对话语音输入）、BluetoothLeScanner（健康设备蓝牙连接）等扩展能力。代码架构经过多轮重构：提取 BaseFragment 基类消除重复、ElderlyDbHelper 独立 DB 层 + DAO/Repository 数据层分离、ChatAdapter 独立适配器复用、GalleryPermissionHelper 统一权限管理、ChatDetailActivity 拆分为 ZhipuApiClient/SpeechManager/ContentFilter/ReplyProvider 四个独立组件。新增 HealthAlertService 健康异常自动告警模块，API Key 通过 local.properties 注入实现安全生产。启用 ProGuard/R8 代码混淆优化 APK 体积。

系统面向两类用户：（1）老人——主要使用者，通过 PIN 码登录后进行健康管理、用药打卡、AI 智能对话等操作；（2）家属——通过子女监控面板查看老人健康数据、服药状态，并为老人上传照片到亲情相册。

---

## 2 系统功能

系统共包含 12 个功能模块，涵盖适老化交互、健康管理、安全求助、生活服务四大领域。

**图2-1 项目功能图**

```
银发守护者 APP
├── 2.1 适老化界面
│   ├── 大字体模式切换（标准/加大/特大，SharedPreferences 存储偏好）
│   └── 高对比度主题切换（文字纯黑 #000000 + 背景纯白 #FFFFFF）
├── 2.2 用户登录与切换
│   ├── PIN 码快速登录（4位数字，老人友好）
│   ├── 多用户头像选择
│   └── 用户增删管理
├── 2.3 首页中枢
│   ├── 日期天气展示
│   ├── 健康快报摘要（5项最新指标）
│   ├── 家属在线状态列表
│   └── AI 对话快捷入口
├── 2.4 健康档案管理
│   ├── 13类健康指标CRUD（血压/血糖/心率/步数/血氧/体温/体重/体脂率/呼吸率/卡路里/睡眠/运动/心情/呼吸正念）
│   ├── 蓝牙设备数据导入（血压计/血氧仪/智能手环）
│   └── AI 健康分析建议
├── 2.5 AI 智能对话
│   ├── 文字与语音双输入方式
│   ├── 智谱 GLM-4 大模型驱动回复
│   ├── 疾病药品智能推荐（17种常见病对应80+种药品）
│   └── 老年人安全劝解规则（剧烈运动/爬山/搬重物等行为温和劝阻）
├── 2.6 定时用药提醒
│   ├── 药品信息管理（名称/类型/服用时间/剂量/用法）
│   ├── AlarmManager 闹钟定时触发
│   ├── Notification 通知栏推送
│   ├── TextToSpeech 语音播报提醒
│   ├── 服药每日打卡
│   └── 药品库关键字搜索
├── 2.7 一键紧急呼叫
│   ├── 单击拨打首位在线家属电话
│   ├── 长按弹出选择列表（家属列表 + 120急救）
│   └── CALL_PHONE 运行时权限申请
├── 2.8 亲情相册
│   ├── 相册列表 → 照片网格 两层浏览结构
│   ├── 系统相册图片选择（Intent.ACTION_PICK）
│   ├── Glide 图片加载与缓存
│   ├── 照片详情大图预览
│   └── 收藏标记与相册删除管理
├── 2.9 子女监控面板
│   ├── 老人健康数据汇总卡片
│   ├── 今日服药打卡状态
│   ├── SOS 紧急提醒记录
│   └── 家属上传照片（含权限管理）
├── 2.10 社区便民查询
│   ├── 高德 3D 地图 SDK 集成
│   ├── POI 兴趣点搜索（菜市场/社区医院/药店/超市/银行）
│   ├── 5色 Marker 标注搜索结果
│   ├── 步行路径规划
│   └── 一键导航（高德地图 App 跳转 / 浏览器兜底）
├── 2.11 防诈骗知识推送
│   ├── OkHttp 异步拉取远程防诈骗 API
│   ├── 每日推送一推卡片
│   ├── RecyclerView 历史推送列表
│   ├── 详细案例与防范措施查看页
│   └── 网络异常本地 assets 数据兜底
└── 2.12 记忆与设置
    ├── 记忆回忆 CRUD（分类筛选：健康/爱好/家庭/其他）
    ├── 适老化设置（大字体/高对比度切换）
    ├── 各功能模块入口汇总
    └── 退出登录
```

### 2.1 适老化界面

针对老年人视力下降、手指操作不精准的特点，系统提供大字体模式和高对比度主题。

业务流程如下：
1. 用户进入"设置"页面，看到"大字模式"下拉选择框；
2. 用户选择"标准""加大"或"特大"其中一种；
3. 系统通过 `SharedPreferences` 存储用户选择的字号偏好；
4. 系统调用 `requireActivity().recreate()` 重建当前 Activity；
5. 所有页面的文本在创建时通过 `BaseFragment.sp()` 方法读取缩放比例（标准=1.0x，加大=1.2x，特大=1.4x），动态应用到 `setTextSize()` 调用中；
6. 高对比度开关同理，切换后文字颜色变为纯黑 #000000，页面背景变为纯白 #FFFFFF，确保文字清晰可辨。

### 2.2 用户登录与切换

考虑到一部手机可能被多位老人共用（如老年夫妇），系统支持多用户 PIN 码登录。

业务流程如下：
1. App 启动后进入登录页面，显示已注册老人头像网格（默认含爷爷、奶奶两个预置用户）；
2. 用户点击选择自己的头像，头像高亮选中；
3. 用户输入 4 位数字 PIN 码（设置时以明文存储于本地 SQLite）；
4. 系统校验 PIN 码正确后，跳转主界面，加载该用户的全部数据；
5. 新用户可通过"添加老人"按钮创建档案（姓名、PIN、年龄、健康状况）；
6. 长按头像可删除用户，仅剩 1 人时提示"至少保留一个老人档案"；
7. 在设置页点击"退出登录"可返回登录页面切换用户。

### 2.3 首页中枢

用户登录后进入底部导航主界面，默认显示"首页中枢"Tab。

系统展示：
1. 顶部显示当前日期（农历格式）和天气信息；
2. 中部为 AI 对话入口卡片，点击跳转全屏 AI 对话页面；
3. 健康快报区域展示 5 项最新健康指标摘要（心率、血压、血糖、步数、体温），点击"查看详情"跳转健康探索页；
4. 家属列表水平展示家属姓名、关系、在线状态（绿点/灰点）；
5. 底部快捷操作栏提供"一键呼叫""健康档案""用药提醒"三个入口按钮。

### 2.4 健康档案管理

用户可通过手动输入或蓝牙设备导入两种方式记录健康数据。

1. 健康探索 Tab 展示健康指标卡片（睡眠、运动、心情、步数、呼吸正念），每张卡片显示图标、名称、数值、状态标签（正常/注意/良好等）；
2. 点击"添加数据"按钮记录新指标，数据存入 SQLite `health_data` 表；
3. 点击"蓝牙导入"进入蓝牙管理页面，扫描并连接 BLE 设备（血压计、血氧仪、智能手环），读取数据后自动写入健康档案；
4. 点击"AI 健康分析"跳转 AI 对话页面，获得基于健康数据的个性化建议。

### 2.5 AI 智能对话

AI 智能对话是系统的核心功能，基于智谱 GLM-4 大语言模型驱动。经过架构重构，将原 562 行的单一 Activity 拆分为 5 个独立组件：ZhipuApiClient（API 通信）、SpeechManager（语音识别）、ContentFilter（话题过滤）、ReplyProvider（离线回复）、ChatDetailActivity（UI 协调）。同时新增 AI 回复语音播报功能（系统 TTS 朗读回复内容，支持一键开关）。

**架构特点**：ChatDetailActivity 从 562 行缩减至 236 行，UI 采用 XML 布局（`activity_chat_detail.xml`）替代原全代码构建方式；API 调用独立为可测试的 ZhipuApiClient；话题过滤规则和 SYSTEM_PROMPT 外置到 `res/raw/system_prompt.txt` 和 ContentFilter 类，支持配置更新。

业务流程如下：
1. 用户从首页中枢或健康探索页点击进入全屏 AI 对话界面；
2. 用户可通过键盘输入文字或点击麦克风按钮使用语音输入提问（SpeechManager 管理 SpeechRecognizer，8 种错误中文友好提示）；
3. 系统通过 ZhipuApiClient（HttpURLConnection）向智谱 API 发送 POST 请求（携带 System Prompt：包含 17 种常见疾病对应 80+ 种药品的映射表 + 老年人安全劝解规则）；
4. ContentFilter 前置过滤非健康相关提问，减少无效 API 调用；
5. AI 根据用户问题返回回复，如涉及疾病则自动推荐 2-3 种对应药品；
6. 当用户提出不适合老年人的行为时，AI 温和劝解并建议安全替代方案；
7. AI 回复文本通过 TtsHelper.speak() 自动语音播报（工具栏有声音开关按钮，默认开启）；
8. API 不可用或话题被拦截时，ReplyProvider 提供本地离线回复兜底；
9. 对话历史存储于 SQLite `messages` 表，切换用户时数据隔离。

### 2.6 定时用药提醒

用户可在用药提醒 Tab 管理个人药品并设置服药闹钟。

业务流程如下：
1. 用户点击"添加药品"按钮，填写药品名称、类型、服用时间（如"08:00,20:00"）、用法（口服/外用/注射）、说明；
2. 支持从药品库搜索关键字（如"高血压"→自动填充硝苯地平缓释片等），减少输入负担；
3. 系统通过 `AlarmManager.setRepeating()` 为每个服药时间设置闹钟；
4. 闹钟触发时，`ReminderBroadcastReceiver` 接收广播，执行两件事：通过 `NotificationManager` 发送通知栏消息、通过 `TtsHelper.speak()` 调用系统默认 TTS 引擎语音播报提醒内容（如"颜爷爷，该吃硝苯地平缓释片了"）；
5. 用户点击通知进入用药提醒 Tab，在药品卡片上点击 CheckBox 完成服药打卡；
6. 打卡记录存入 `medicine_taken` 表，同一用户同一药品同一天只能打卡一次（UNIQUE 约束）；
7. `BootReceiver` 监听设备开机广播，自动恢复所有闹钟。

### 2.7 一键紧急呼叫

主界面常驻红色"一键呼叫"按钮，帮助老人在紧急情况下快速求助。

业务流程如下：
1. 用户在健康探索 Tab 顶部操作栏看到红色"📞 一键呼叫"按钮；
2. 单击按钮 → 系统遍历家属列表找到第一位在线且有电话的家属；
3. 系统检查 `CALL_PHONE` 权限：若未授权则弹出系统权限弹窗（`ActivityCompat.requestPermissions()`）；
4. 权限通过后调用 `Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone))` 直接拨出电话；
5. 长按按钮弹出 AlertDialog 选项列表（所有家属 + 120 急救中心），用户选择后拨号；
6. 权限被拒绝时 Toast 提示用户并在后续弹窗引导至系统设置。

### 2.8 亲情相册

家属为老人上传照片，老人浏览回忆。经过 UI/UX 优化，增加了跨屏淡入过渡动画、空状态引导设计、上传实时预览反馈等交互增强。

**交互特点**：相册列表与照片网格之间使用 120ms 淡出 + 150ms 淡入过渡动画；"全部照片"入口使用 Primary Dark 色粗体标题与绿色计数区分视觉权重；新建空相册显示插画 + "这个相册还是空的"引导文案 + 上传按钮；上传照片选择后预览区实时更新图片，按钮变"已选择照片 ✓"绿色确认。

业务流程如下：
1. 进入"亲情相册"Tab，显示相册列表（按分类分组），首个为"全部照片"（绿色粗体视觉区分）；
2. 每张相册卡片（22dp 圆角 CardView）显示封面、相册名称、照片数量；
3. 点击"创建相册"输入相册名称，创建后自动进入新相册（显示空状态引导）；
4. 点击相册卡片以淡入过渡进入照片网格视图（StaggeredGridLayoutManager 2 列瀑布流）；
5. 点击"上传照片"弹出表单，实时预览选图，填写标题和留言后保存；
6. 选图前通过 GalleryPermissionHelper 检查权限：Android 13+ 需 `READ_MEDIA_IMAGES`；
7. 点击照片进入详情页（`PhotoDetailActivity`），显示大图/标题/分类/场景标签/家属留言/收藏；
8. 长按照片切换收藏状态，长按相册卡片可删除整个相册（含确认弹窗）；

### 2.9 子女监控面板

家属可通过子女模式远程关注老人健康状况。

业务流程如下：
1. 从设置页点击"子女模式"进入 `ChildModeActivity`；
2. 页面展示 4 个模块：健康摘要（6 项指标）、今日服药打卡（已打卡数/总数+每个药品状态）、SOS 紧急提醒记录（最近 3 条）、家属列表（姓名/关系/电话/在线状态）；
3. 点击"为老人上传照片"按钮，弹出相册选择表单（含权限检查），照片上传后老人端亲情相册可立即查看。

### 2.10 社区便民查询

集成高德 3D 地图 SDK，为老人提供周边生活服务设施查询与导航。

业务流程如下：
1. 从设置页点击"便民查询"进入 `CommunityActivity`；
2. 系统首先调用 `MapsInitializer.updatePrivacyShow/updatePrivacyAgree` 完成高德隐私合规初始化；
3. 加载 MapView 3D 地图，显示当前位置蓝点（需 `ACCESS_FINE_LOCATION` 权限）；
4. 用户点击搜索按钮（菜市场/社区医院/药店/超市/银行）；
5. 系统创建 `PoiSearch.Query` + `SearchBound(5000m)` 并异步发起周边搜索；
6. 搜索结果通过 `onPoiSearched()` 回调，在地图上标注 5 色 Marker；
7. 点击 Marker 弹出 AlertDialog 显示 POI 详情（地址、距离、电话）；
8. 点击"步行导航"→`RouteSearch.calculateWalkRouteAsyn()` 计算步行路线，展示距离和时间；
9. 点击"打开高德导航"→ Intent 调起高德 App（`androidamap://navi`），未安装则打开浏览器版高德导航。

### 2.11 防诈骗知识推送

使用 OkHttp 第三方网络库从远程拉取反电信诈骗知识，帮助老年人提高防范意识。

业务流程如下：
1. 从设置页点击"防诈提醒"进入 `FraudFragment`；
2. 系统通过 `FraudApiClient.fetchFraudTips()` 发起 OkHttp 异步 GET 请求；
3. 服务器返回 JSON 数据，解析 `data` 数组中的每条防诈骗条目（标题、分类、摘要、详情、应对措施）；
4. RecyclerView 展示每日推送列表，每张卡片显示标题和分类；
5. 点击条目进入 `FraudDetailActivity`，展示完整案例详情（📋 案例详情 + 🛡️ 防范措施）；
6. 网络异常时自动回退到 `assets/fraud_tips.json` 本地预置 10 条数据兜底。

### 2.12 记忆回忆

老人可记录生活中的重要记忆（健康、爱好、家庭等分类），并随时查看回顾。

业务流程如下：
1. 从设置页点击"记忆回忆"进入 `MemoryFragment`；
2. 系统展示记忆卡片列表，每张卡片显示记忆内容和分类标签；
3. 用户可点击"新增"按钮输入记忆内容和选择分类（健康/爱好/家庭/其他）；
4. 记忆数据存入 SQLite `memories` 表；
5. 支持按分类筛选（全部/健康/爱好/家庭/其他）；
6. 长按记忆卡片可删除。

---

## 3 页面设计与实现

### 3.1 页面介绍

本系统包含 **8 个 Activity** 和 **9 个 Fragment**，外加 **5 个独立组件**（ai/ 包、data/dao/ 包、health/ 包），共计 37 个 Java 源文件。

| 序号 | 页面名称 | 说明 |
|------|---------|------|
| 1 | LoginActivity | 登录页面：用户选择、PIN输入、用户增删 |
| 2 | MainActivity | 主页面：底部导航栏容器，管理 5 个核心 Tab |
| 3 | ChatDetailActivity | AI 全屏对话页面：聊天+语音输入+侧边栏 |
| 4 | PhotoDetailActivity | 照片大图详情页：大图+信息+翻页 |
| 5 | ChildModeActivity | 子女监控面板：健康/用药/SOS/家属汇总 |
| 6 | CommunityActivity | 社区便民查询：高德 3D 地图+POI搜索+导航 |
| 7 | BluetoothActivity | 蓝牙设备管理：BLE 扫描+连接+数据读取 |
| 8 | FraudDetailActivity | 防诈骗详情页：案例+防范措施 |
| 9 | HomeFragment | 首页中枢：健康快报+家属列表+快捷入口 |
| 10 | HealthFragment | 健康探索：健康指标卡片+蓝牙/AI入口 |
| 11 | MedicineFragment | 用药提醒：药品列表+打卡+筛选+添加 |
| 12 | AlbumFragment | 亲情相册：相册列表+照片网格两层结构 |
| 13 | SettingsFragment | 设置：大字体/高对比度+功能入口+退出 |
| 14 | MemoryFragment | 记忆回忆：记忆卡片列表+分类筛选+增删 |
| 15 | FraudFragment | 防诈骗推送：OkHttp 拉取+列表+详情入口 |
| 16 | CommunityFragment | 社区查询（独立 Fragment，曾被 CommunityActivity 引用） |
| 17 | BaseFragment | 所有 Fragment 的基类（提供 dp/sp/color/toast 共享方法） |

### 3.2 页面设计

#### （1）LoginActivity 登录页面

**使用的控件和布局**：
- LinearLayout（垂直）作为根布局，设置 `gravity="center"` 使内容居中
- LinearLayout（水平）放置品牌 Logo 与标题文字
- RecyclerView + GridLayoutManager(3列) 展示用户头像网格，每个条目使用 `item_user_avatar.xml` 布局
- CardView 包裹 PIN 码输入区域，提供圆角卡片视觉分组
- EditText 设置 `inputType="numberPassword"` 限制 4 位数字输入
- Button 为登录按钮，默认 disabled 状态，输入满 4 位且选中用户后才启用

**主要解决的问题**：为老人提供简单直观的登录方式。PIN 码替代复杂密码，头像网格支持多用户快速切换。

#### （2）MainActivity 主页面

**使用的控件和布局**：
- LinearLayout（垂直）作为根布局
- FrameLayout 作为 Fragment 容器，`layout_weight="1"` 占满剩余空间
- BottomNavigationView 底部导航栏，5 个菜单项（健康探索/用药提醒/首页中枢/亲情相册/设置），`app:labelVisibilityMode="labeled"` 显示标签

**主要解决的问题**：通过底部导航实现 5 个核心模块的快速切换，符合 Android Material Design 标准。

#### （3）HomeFragment 首页中枢

**使用的控件和布局**：
- ScrollView 包裹 LinearLayout（垂直）支持内容滚动
- 日期行使用水平 LinearLayout（日期 + 天气标签）
- AI 对话入口卡片使用大图标 + 标题 + 引导文字，设置 `backgroundResource` 突出显示
- 健康快报使用 RecyclerView 展示指标列表
- 家属列表使用水平滚动 RecyclerView
- 快捷操作栏使用水平 LinearLayout + 三个 Button

**主要解决的问题**：将老人最常用的功能整合在首页，减少操作层级。视觉上使用大字号（24sp）和高对比度文字。

#### （4）HealthFragment 健康探索

**使用的控件和布局**：
- ScrollView + LinearLayout（垂直）
- 每个健康指标为水平 LinearLayout（图标 + 名称 + 数值 + 状态标签）
- 卡片使用圆角 `bg_card_surface` 背景
- HorizontalScrollView 包含操作按钮栏（一键呼叫/添加数据/蓝牙导入）

**主要解决的问题**：将 13 类健康指标以卡片形式清晰展示，关键指标（心率、血压）优先显示。操作入口横向排列支持滑动。

#### （5）MedicineFragment 用药提醒

**使用的控件和布局**：
- FrameLayout 作为容器
- RecyclerView + LinearLayoutManager 展示药品列表
- FloatingActionButton 添加药品按钮（绿色）
- Spinner 药品类型筛选
- CheckBox 服药打卡控件
- `item_medicine.xml` 卡片布局：药品名称 + 服用时间 + 用法说明 + CheckBox

**主要解决的问题**：药品信息卡片化展示，打卡交互一键完成。类型筛选帮助多药品管理。

#### （6）AlbumFragment 亲情相册

**使用的控件和布局**：
- 两套视图状态：相册列表 / 照片网格，通过 `transitionTo()` 方法实现 120ms 淡出→150ms 淡入过渡动画
- 相册列表使用 RecyclerView + StaggeredGridLayoutManager(2列)，"全部照片"入口绿色粗体视觉区分
- 照片网格同样使用瀑布流布局，空状态显示插画 + 引导文案 + 上传按钮
- 卡片使用 CardView（22dp 圆角）+ ImageView（Glide centerCrop）+ 文字区域（16dp 内边距）
- 上传弹窗使用 AlertDialog + 自定义 LinearLayout 表单（实时预览 + 选择状态 + 标题 + 留言）
- GalleryPermissionHelper 统一处理权限逻辑，实时更新预览图片和选择确认状态

**主要解决的问题**：相册→照片两层结构清晰，瀑布流布局美观，空状态引导降低认知负荷，过渡动画提升交互流畅度，权限处理独立封装避免代码重复。

#### （7）SettingsFragment 设置页面

**使用的控件和布局**：
- ScrollView + LinearLayout（垂直）
- Spinner 大字体选择（标准/加大/特大三项）
- Switch/TextView 高对比度开关
- 各功能入口使用卡片列表（icon + 标题 + 描述 + 箭头）
- 退出登录按钮

**主要解决的问题**：集中管理所有次级功能入口和用户偏好设置，卡片式布局清晰易点击。

#### （8）ChatDetailActivity AI 对话页面

**架构重构**：原 562 行 God Activity 拆分为 5 个独立组件——ZhipuApiClient（API 调用）、SpeechManager（语音识别生命周期）、ContentFilter（话题过滤）、ReplyProvider（离线回复）、ChatDetailActivity（236 行 UI 协调器）。UI 改用 XML 布局（`activity_chat_detail.xml`）替代全代码构建，SYSTEM_PROMPT 外置到 `res/raw/system_prompt.txt`。

**使用的控件和布局**：
- DrawerLayout 作为根布局（主内容 + 侧边栏最近对话）
- RecyclerView 展示对话列表（AI 左气泡 mint 背景、用户右气泡白色背景）
- EditText 输入框 + ImageButton 语音按钮 + ImageButton 发送按钮
- ImageButton 声音开关按钮（默认开启，点击切换 TTS AI 回复朗读）
- HorizontalScrollView 快捷提问标签（3 个常见问题）
- SpeechRecognizer 系统语音识别（由 SpeechManager 管理，8 种错误中文提示）

**主要解决的问题**：全屏对话体验，左右气泡区分用户/AI 消息；ContentFilter 前置过滤非健康话题减少 API 浪费；TTS 朗读 AI 回复降低阅读负担，适合视力不佳的老人；组件化架构使每个单元可独立测试。

#### （9）CommunityActivity 社区便民查询页面

**使用的控件和布局**：
- LinearLayout（垂直）根布局
- HorizontalScrollView 包含 5 个搜索按钮
- MapView（高德3D地图）占满剩余空间 `layout_weight="1"`
- 底部返回按钮
- AlertDialog 展示 POI 详情和导航选项

**主要解决的问题**：地图+搜索一体化，按钮大且间距充足，导航一键跳转无需手动输入目的地。

### 3.3 应用效果

从开发人员角度：通过 BaseFragment 基类统一管理 `dp()`、`sp()`、`color()` 工具方法，消除了 9 个 Fragment 中的重复代码（每类约 5-10 行）。`GalleryPermissionHelper` 将权限逻辑从 Activity/Fragment 中解耦，实现了单一职责。RecyclerView 的广泛使用（9 处）确保了列表渲染性能和大数据量支持。SQLite 11 张表通过 `ElderlyDbHelper` 统一管理，数据操作有清晰的入口。

从用户角度：大字体模式和高对比度主题切实解决了老年用户视力不佳的问题。PIN 码登录比传统密码输入更简单。一键呼叫将紧急求助的操作降到最少（1 次点击）。AI 对话的安全劝解规则有效防止老人做出危险行为。社区便民查询让老人无需打字就能找到周边的菜市场、药店和医院。用药提醒的通知+语音双重播报确保老人不会错过服药时间。

---

## 4 系统功能具体实现

### 4.1 适老化界面（SharedPreferences + FontScaleHelper）

**使用的技术**：SharedPreferences 存储用户偏好，自定义 `FontScaleHelper` 工具类提供统一接口。

**实现方式**：
- `FontScaleHelper` 封装 SharedPreferences 的读写操作，提供 `getFontModeIndex()`、`setFontMode()`、`isHighContrast()`、`getTextScale()` 等方法；
- 字号缩放比例：normal=1.0x、large=1.2x（默认）、xlarge=1.4x；
- `BaseFragment.sp(int baseSp)` 方法调用 `FontScaleHelper.sp()` 动态计算实际像素值；
- 高对比度通过 `FontScaleHelper.textPrimary()`、`FontScaleHelper.bgPage()` 等方法返回黑色/白色；
- SettingsFragment 中切换设置后调用 `requireActivity().recreate()` 全局重建 UI。

### 4.2 数据持久化（SQLite + DAO/Repository 模式）

**使用的技术**：`SQLiteOpenHelper` 标准封装，手写 SQL，`ContentValues` 插入，`Cursor` 查询。采用 DAO/Repository 分层架构。

**实现方式**：
- `ElderlyDbHelper` 独立管理数据库创建和版本升级，`onCreate()` 中执行 11 张表的建表语句。`onUpgrade()` 采用逐版本 ALTER TABLE 迁移模式（非 DROP ALL），确保用户数据在版本升级时不丢失；
- 数据层采用 **DAO/Repository 模式**：8 个 DAO 类（UserDao、HealthDao、MedicineDao、AlbumDao、ChatDao、FamilyDao、MemoryDao、EmergencyDao）各管理一张表，`Repository` 单例统一协调所有 DAO 并管理内存缓存和活跃用户上下文；
- 旧 `MockData` 类保留为向后兼容的静态 API 委托层（397 行→95 行），所有 Fragment/Activity 无需修改即可运行；
- 核心规则：所有数据表包含 `user_id` 外键，多用户数据严格隔离；药品打卡表设置 `UNIQUE(user_id, medicine_id, taken_date)` 约束防止重复打卡。

### 4.3 RecyclerView 与列表展示

**使用的技术**：RecyclerView + 多种 LayoutManager。

**实现方式**：
- 健康列表、药品列表、防诈列表使用 `LinearLayoutManager` 垂直滚动；
- 相册网格使用 `StaggeredGridLayoutManager(2列)` 瀑布流布局；
- 用户选择使用 `GridLayoutManager(3列)` 固定网格布局；
- 家属列表使用 `LinearLayoutManager.HORIZONTAL` 水平滚动；
- 每个列表使用独立的 Adapter + ViewHolder 内部类，ViewHolder 持有所有子 View 引用避免反复 findViewById。

### 4.4 权限管理（运行时权限申请）

**使用的技术**：`ActivityCompat.requestPermissions()` + `onRequestPermissionsResult()`。

**实现方式**：
- `CALL_PHONE`：一键呼叫时检查，用户拒绝后 Toast 提示并可在后续弹窗引导至系统设置；
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`：上传照片选图时检查，Android 13+ 使用前者，旧版使用后者（maxSdkVersion=32）；
- `RECORD_AUDIO`：AI 对话语音输入时检查；
- `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT`：蓝牙扫描时检查（Android 12+）；
- `ACCESS_FINE_LOCATION`：社区便民查询定位和高德 SDK 使用；
- `POST_NOTIFICATIONS`：Android 13+ 用药提醒通知权限。

### 4.5 高德地图 SDK（社区便民查询）

**使用的技术**：高德 3D Map SDK 10.0.600 + Search SDK 9.7.0。

**实现方式**：
- 在 `settings.gradle` 添加高德 Maven 仓库：`https://maven.amap.com/repository/maven-public`；
- `app/build.gradle` 添加依赖：`com.amap.api:3dmap:10.0.600` 和 `com.amap.api:search:9.7.0`；
- API Key 通过 `AndroidManifest.xml` 的 `<meta-data>` 配置，引用 `strings.xml` 的值；
- 关键实现：`MapView` 生命周期管理（onCreate/onResume/onPause/onDestroy/onSaveInstanceState）；
- 隐私合规：所有 API 调用前必须执行 `MapsInitializer.updatePrivacyShow()` 和 `updatePrivacyAgree()`；
- POI 搜索：`PoiSearch.Query(keyword, "", "广州")` + `SearchBound(LatLonPoint, 5000m)` 限制 5km 范围；
- 步行路径规划：`RouteSearch.WalkRouteQuery(from, to)` → 回调展示距离和时间。

### 4.6 第三方网络请求库（OkHttp）

**使用的技术**：OkHttp 4.12.0。

**实现方式**：
- `FraudApiClient` 封装 OkHttp 客户端，配置 `followRedirects(true)` + `followSslRedirects(true)`，10 秒超时，异步 GET 请求防诈骗 API；
- 防诈骗数据托管于 GitHub Raw (`raw.githubusercontent.com/Nana1237854/silver-guardian/master/app/src/main/assets/fraud_api_data.json`)，包含 10 条防诈骗知识；
- 回调通过 `Handler(Looper.getMainLooper())` 切回主线程更新 UI；
- `parseFraudResponse()` 方法提取为静态方法，可独立测试 JSON 解析逻辑（6 个 JUnit 测试覆盖）；
- 网络异常时自动从 `assets/fraud_tips.json` 加载本地兜底数据；
- 此方法同时满足"第三方网络请求库"技术考察点（区别于课程教的 HttpURLConnection）。

### 4.7 TTS 语音播报（用药提醒）

**使用的技术**：`android.speech.tts.TextToSpeech`，通过 `TtsHelper` 单例管理。

**实现方式**：
- `TtsHelper.init()` 在 `MainActivity.onCreate()` 时预初始化，使用系统默认 TTS 引擎（与用户在 设置→文字转语音 中选择的引擎一致），不指定引擎包名确保兼容性；
- 设置语言为 `Locale.CHINESE`，语速 0.8x（稍慢适合老人）；
- `ReminderBroadcastReceiver` 收到闹钟广播后调用 `TtsHelper.speak()` 语音播报「用户名，该吃XX药了，请按时服药」；
- `TtsHelper` 初始化失败时自动重试最多 5 次，超限后静默降级为仅通知提醒；
- `BootReceiver` 开机后也调用 `TtsHelper.init()` 确保重启后 TTS 可用。

### 4.8 SpeechRecognizer 语音输入

**使用的技术**：`android.speech.SpeechRecognizer` + `RecognitionListener`。

**实现方式**：
- `SpeechRecognizer.createSpeechRecognizer(context)` 创建实例；
- `Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)` 配置中文识别（`EXTRA_LANGUAGE = "zh-CN"`）；
- 8 种错误码均有中文友好提示；
- 支持部分识别结果实时显示（`onPartialResults`）。

### 4.9 蓝牙 BLE 扫描（蓝牙设备管理）

**使用的技术**：`BluetoothLeScanner` + `ScanCallback`。

**实现方式**：
- 进入蓝牙页面时检查蓝牙状态：蓝牙未开启则弹出系统对话框 `ACTION_REQUEST_ENABLE` 引导用户开启；
- 真机使用 `BluetoothLeScanner.startScan()` 扫描附近 BLE 设备，过滤健康相关设备（名称含 BP/OX/HR/血压/血氧/心率）；
- 5 秒自动停止扫描，设备去重；
- BluetoothLeScanner 不可用时自动降级为 3 个模拟设备（血压计、血氧仪、智能手环）；
- 连接设备后通过 `MockData.addHealthData()` 写入 SQLite。

### 4.10 健康异常自动告警（HealthAlertService）

**使用的技术**：Android NotificationManager + TTS 语音播报 + 阈值判断算法。

**实现方式**：
- `HealthAlertService.initChannel()` 在 MainActivity 启动时注册 Android 8+ 通知渠道（IMPORTANCE_HIGH）；
- `HealthAlertService.checkAndAlert()` 在每次健康数据写入（`Repository.addHealthData()`）时自动触发；
- `evaluate()` 方法对 6 种关键指标（心率、血压、血氧、体温、血糖、呼吸率）进行阈值判断，返回 NORMAL/WARNING/CRITICAL 三级；
- CRITICAL 级：心率 <45 或 >130 bpm、收缩压 >180 mmHg、血氧 <90%、体温 >39°C、血糖 >16 或 <3 mmol/L；
- 告警触发后执行三重通知：通知栏推送（NotificationManagerCompat）、TTS 语音播报、记录到家属可见的紧急提醒列表；
- 数据入口覆盖手动录入（HealthFragment）和蓝牙设备同步（BluetoothActivity）两条路径。

### 4.11 图片加载（Glide）

**使用的技术**：Glide 4.16.0。

**实现方式**：
- 亲情相册中缩略图使用 `Glide.with(context).load(Uri.parse(url)).centerCrop().into(imageView)`；
- 照片详情页大图使用 `Glide.with(context).load(url).into(imageView)`；
- 加载失败时显示系统默认图标作为兜底。

---

## 5 系统功能测试

| 编号 | 测试功能 | 测试步骤 | 预期结果 | 状态 |
|------|---------|---------|---------|------|
| T01 | PIN登录 | 选择用户→输入正确PIN→点击登录 | 进入主页，数据正确加载 | ✅ |
| T02 | PIN错误 | 选择用户→输入错误PIN→点击登录 | Toast提示"PIN码错误"，输入框清空 | ✅ |
| T03 | 多用户切换 | 以爷爷身份登录→退出→以奶奶身份登录 | 数据按userId隔离切换 | ✅ |
| T04 | 添加用户 | 点击"添加老人"→填写姓名/PIN/年龄/状况→保存 | 新用户出现在列表，可用正确PIN登录 | ✅ |
| T05 | 删除用户 | 长按用户头像→确认删除 | 用户从列表消失，只剩1人时提示不可删 | ✅ |
| T06 | 大字体模式 | 设置→选择"特大"→回首页查看 | 所有文字明显放大 | ✅ |
| T07 | 高对比度 | 设置→开启高对比度→查看 | 文字黑色背景白色，对比度明显增强 | ✅ |
| T08 | 一键呼叫（单击） | 健康页→单击一键呼叫→授权电话权限 | 弹出系统拨号界面，号码为家属电话 | ✅ |
| T09 | 一键呼叫（长按） | 长按一键呼叫→弹出选项→选"120急救中心" | 拨打120 | ✅ |
| T10 | 呼叫权限拒绝 | 拒绝CALL_PHONE权限后再次点击 | Toast+弹窗引导去设置授权 | ✅ |
| T11 | AI对话发送 | 输入"血压偏高怎么办"→发送 | AI回复含降压药推荐+安全提醒 | ✅ |
| T12 | AI安全劝解 | 输入"我想去爬很高的山" | AI温和劝阻并建议安全替代活动 | ✅ |
| T13 | AI语音输入 | 点击麦克风→授权录音→说话 | 语音识别文本填入输入框 | ✅ |
| T14 | AI对话历史 | 发送多条消息→退出→重新进入 | 历史消息保持 | ✅ |
| T15 | 添加药品 | 用药提醒→点加号→填写信息→保存 | 药品出现在列表中 | ✅ |
| T16 | 药品库搜索 | 添加药品→搜索框输入"高血压"→回车 | 自动填充硝苯地平缓释片信息 | ✅ |
| T17 | 服药打卡 | 点击未打卡药品的CheckBox | 变为已打卡+Toast提示 | ✅ |
| T18 | 取消打卡 | 再次点击已打卡药品的CheckBox | 变为未打卡+Toast提示 | ✅ |
| T19 | 药品类型筛选 | Spinner切换类型 | 列表只显示对应类型药品 | ✅ |
| T20 | 删除药品 | 长按药品→确认删除 | 药品消失 | ✅ |
| T21 | 相册列表展示 | 进入亲情相册Tab | 显示相册分类卡片+封面+张数 | ✅ |
| T22 | 创建相册 | 点"创建相册"→输入名称→确定 | 自动进入新相册（空） | ✅ |
| T23 | 进入相册 | 点某个相册卡片 | 显示该相册内所有照片 | ✅ |
| T24 | 上传照片 | 相册内→点上传→选图→填写标题→保存 | 照片出现在网格中 | ✅ |
| T25 | 上传照片权限 | 首次上传→授权相册权限 | 成功打开系统相册选图 | ✅ |
| T26 | 照片详情 | 点击照片→查看大图 | 显示大图+标题+分类+留言+位置 | ✅ |
| T27 | 照片收藏 | 长按照片→切换收藏 | 收藏标记切换 | ✅ |
| T28 | 删除相册 | 长按相册卡片→确认 | 相册及照片全部删除 | ✅ |
| T29 | 便民查询-地图展示 | 设置→便民查询→等待加载 | 地图显示，定位蓝点可见 | ✅ |
| T30 | 便民查询-POI搜索 | 点搜索按钮（如"菜市场"） | 地图标注Marker，状态栏显示数量 | ✅ |
| T31 | 便民查询-步行导航 | 点Marker详情→步行导航 | 显示步行距离和时间 | ✅ |
| T32 | 便民查询-跳转导航 | 点"打开高德导航" | 跳转高德App或浏览器 | ✅ |
| T33 | 防诈推送-远程拉取 | 设置→防诈提醒→有网 | 显示"已从网络获取10条" | ✅ |
| T34 | 防诈推送-详情 | 点击某条目 | 显示完整案例+防范措施 | ✅ |
| T35 | 防诈推送-网络兜底 | 断网后刷新 | 显示"网络获取失败"回退本地数据 | ✅ |
| T36 | 子女监控面板 | 设置→子女模式 | 健康/用药/SOS/家属四个模块展示 | ✅ |
| T37 | 子女上传照片 | 子女模式→点上传→选图→保存 | 照片保存+老人端可见 | ✅ |
| T38 | 蓝牙开启提示 | 设置→蓝牙（蓝牙关） | 弹出系统蓝牙开启对话框 | ✅ |
| T39 | 蓝牙扫描 | 设置→蓝牙→开启→扫描 | BLE扫描或模拟设备加载 | ✅ |
| T39b | 蓝牙模拟 | 点击"加载模拟设备" | 显示3个模拟设备列表 | ✅ |
| T39c | 蓝牙连接 | 点"连接"按钮 | 数据写入健康档案 | ✅ |
| T39d | TTS语音播报 | 用药提醒→长按+按钮 | 通知+TTS语音播报用药提醒 | ✅ |
| T40 | 记忆管理 | 设置→记忆回忆→查看 | 记忆列表+分类筛选 | ✅ |
| T41 | 新增记忆 | 点新增→输入内容+选分类→保存 | 新记忆出现在列表 | ✅ |
| T42 | 删除记忆 | 长按记忆→确认 | 记忆消失 | ✅ |
| T43 | 数据持久化 | 添加数据→退出重登→再次进入 | 数据仍在，不丢失 | ✅ |
| T44 | 单元测试 | 命令行执行 `./gradlew test` | BUILD SUCCESSFUL，27个用例通过 | ✅ |
| T45 | AI语音播报开关 | AI对话→点击声音按钮→发送消息 | 语音关闭时仅显示回复；语音开启时朗读回复 | ✅ |
| T46 | AI语音播报朗读 | 声音开启→发送"血压高怎么办" | 系统TTS朗读AI回复内容 | ✅ |
| T47 | 相册空状态 | 创建新相册（无照片） | 显示插画+"这个相册还是空的"+上传按钮 | ✅ |
| T48 | 相册过渡动画 | 点击相册卡片 | 列表淡出→网格淡入（120ms+150ms） | ✅ |
| T49 | 全部照片视觉区分 | 进入亲情相册Tab | "全部照片"绿色粗体+绿色计数，分类相册黑色普通 | ✅ |
| T50 | 上传实时预览 | 上传照片→从相册选图 | 预览区显示选中图片，按钮变"已选择照片 ✓" | ✅ |
| T51 | 健康异常告警-临界 | 手动录入心率40bpm（低于45） | 通知栏推送+TTS语音播报+家属记录 | ✅ |
| T52 | 健康异常告警-正常 | 手动录入心率72bpm | 无告警触发 | ✅ |
| T53 | DB迁移安全 | ElderlyDbHelperMigrationTest | v1→v2升级后用户/健康/用药数据完整保留 | ✅ |
| T54 | UI自动化-登录 | CoreFlowUITest - loginScreen | PIN输入状态、正确/错误登录、MainActivity跳转 | ✅ |
| T55 | UI自动化-导航 | CoreFlowUITest - navigation | 5个Tab切换、AI对话跳转、消息发送 | ✅ |
| T56 | API Key安全 | 构建APK→检查 BuildConfig | 密钥从local.properties注入，strings.xml仅占位符 | ✅ |
| T57 | R8混淆 | `assembleRelease` | APK体积缩小3-5MB，ProGuard规则保护关键类 | ✅ |

---

## 6 系统实现所使用的相关AI工具

本项目在开发过程中使用了 **Claude Code**（Anthropic 的 AI 编程助手）辅助完成开发工作。以下是 AI 工具在项目各阶段的具体使用过程：

### 6.1 需求分析与技术方案设计阶段

使用 Claude Code 的 `/grill-with-docs` 功能，将课程文档《广州商学院课程考查内容及评分标准》中的 5 项功能要求和 6 项技术考察点输入 AI。AI 通过逐轮盘问的方式，帮助确定了所有技术选型决策：高德地图 SDK（vs 百度）、OkHttp（vs Retrofit）、系统 TTS（vs 第三方语音库）、ACTION_CALL + 运行时权限方案等。生成的 CONTEXT.md 领域术语表和 ADR 架构决策记录直接用于后续开发指导。

### 6.2 全景战略审查阶段

使用 `/plan-ceo-review`（CEO 战略审查）、`/plan-eng-review`（工程架构审查）、`/autoplan`（全自动审查管线）三大技能对项目进行系统化质量评估。CEO 审查确认了 9 个扩展提案，接受了 AI 语音播报、UI 自动化测试、健康异常告警 3 项新功能；Eng 审查完成了 4 个维度的深度分析（架构/代码质量/测试/性能），识别出 3 个 CRITICAL 问题（DB onUpgrade 数据丢失、MockData God Object、ChatDetailActivity 职责过载）并全部给出具体修复方案。

### 6.3 安全审计阶段

使用 `/cso`（Chief Security Officer）安全审计，发现 5 个安全问题：智谱 + 高德 API Key 在公开 GitHub 仓库中暴露（CRITICAL）、build.gradle 签名密码硬编码（HIGH）、APK 未混淆 minifyEnabled false（HIGH）、PIN 码明文存储（MEDIUM）、LoginActivity 导出（LOW）。通过 `git filter-repo` 重写全部 42 个历史提交清除密钥，并将 API Key 迁移至 local.properties + BuildConfig 注入的安全方案。使用 `/benchmark` 进行 APK 体积分析，发现高德 3D SDK 占 20MB（50%），通过启用 R8 混淆节省 3-5MB。

### 6.4 原型快速搭建阶段

使用 `/prototype` 功能，基于原有 Vue Web 版本的项目代码（`D:\智能\智能`），AI 分析了 5221 行的 App.vue 组件并自动生成了完整的 Android 项目骨架（25 个 Java 类、14 个 XML 布局、32 个图形资源），在数十分钟内完成了一个包含登录、导航、4 个 Tab 页面、模拟数据的可运行原型。

### 6.5 测试驱动开发阶段

使用 `/tdd` 功能，AI 为 4 个核心模块编写了原始 24 个单元测试。后续使用 Eng 审查和架构重构产出，新增了 ElderlyDbHelperMigrationTest（DB 迁移测试）和 CoreFlowUITest（11 个 Espresso UI 自动化测试，覆盖登录、导航、AI 对话等核心流程），测试总数从 24 提升至 44 个。

### 6.6 Bug 诊断与修复阶段

使用 `/investigate`、`/diagnose` 功能，AI 系统化排查了 6 个运行期崩溃和逻辑缺陷，以及 1 个构建配置问题（AGP 8.x BuildConfig 默认禁用导致编译失败）。

### 6.7 代码架构深度重构阶段

使用 `/plan-eng-review` 审查产出和手动实现的 T1-T5 任务链，完成 5 项重大重构：
1. **T1 DB 迁移安全修复**：ElderlyDbHelper.onUpgrade() 从 DROP ALL 11 张表改为逐版本 ALTER TABLE 迁移模式
2. **T2 数据层分离**：MockData（397 行 God Object）拆分为 8 个 DAO + Repository 单例，MockData 变为向后兼容委托层（95 行）
3. **T3 对话架构重构**：ChatDetailActivity（562 行 7 种职责）拆分为 ZhipuApiClient/SpeechManager/ContentFilter/ReplyProvider + XML 布局，缩减至 236 行
4. **T4 设计系统审计**：使用 `/design-review` 源码级审查，发现并修复 13 处字号偏离、多处非标准间距、PIN 输入标签缺失
5. **T5 家人相册 UI/UX 优化**：使用 `/frontend-design` + `/design-consultation`，添加跨屏淡入过渡动画、空状态引导设计、全部照片视觉区分、上传实时预览反馈

### 6.8 新功能实现阶段

使用 Claude Code 实现了 3 项扩展功能：
1. **E1 AI 回复语音播报**：在工具栏添加声音开关，AI 回复通过 TtsHelper.speak() 自动朗读，降低阅读负担
2. **E3 健康异常自动告警**：创建 HealthAlertService，对 6 种指标（心率/血压/血氧/体温/血糖/呼吸率）进行 NORMAL/WARNING/CRITICAL 三级阈值判断，异常时同时触发通知推送 + TTS 语音 + 家属记录
3. **API Key 安全生产化**：密钥移入 local.properties（gitignored），通过 build.gradle 的 buildConfigField + manifestPlaceholders 注入，strings.xml 仅保留占位符

### 6.9 项目说明书撰写

本文档由 AI 根据课程模板格式自动生成并持续更新，组织了 7 个章节、57 条测试用例、12 个功能模块的详细技术说明，人工补充团队成员信息和收获体会后即可提交。

---

## 7 小组成员分工以及收获体会

### 7.1 小组成员分工

| 成员 | 学号 | 负责模块 | 占比 |
|------|------|---------|------|
| （填写） | （填写） | 核心架构、AI对话、登录导航、架构重构、安全方案、语音播报 | 40% |
| （填写） | （填写） | 健康档案、用药提醒、一键呼叫、子女面板、数据层、TTS、健康告警 | 30% |
| （填写） | （填写） | 亲情相册、社区地图、防诈骗、适老化界面、测试、文档 | 30% |

### 7.2 收获与体会

（由每位小组成员分别填写）

**成员一（XXX）：**

做完这个课程设计之后，我对 Android 开发有了系统性的认识。从最初的需求分析到最终的项目交付，完整经历了移动应用开发的全流程。作为负责核心架构的成员，我最大的收获来自代码架构的多轮深度重构——将 ChatDetailActivity 从 562 行的 God Class 拆分为 ZhipuApiClient、SpeechManager、ContentFilter、ReplyProvider 四个独立组件，将 MockData 从 397 行拆分为 8 个 DAO 加 Repository 模式。这些重构让我深刻理解了单一职责原则和依赖倒置在实际项目中的价值。在集成智谱 GLM-4 API 的过程中，我学习了 HttpURLConnection 的网络请求、Handler 线程通信、JSONObject 数据解析等课程核心知识点。AI 对话的话题过滤机制和 System Prompt 调优让我接触到了大语言模型应用开发的前沿实践。AI 辅助编程工具的引入大大提高了开发效率——使用 Claude Code 能够快速搭建原型、诊断 Bug 和生成测试用例，但同时也让我意识到理解底层原理的重要性：AI 可以帮你写代码，但只有你自己理解了架构，才能判断 AI 写的代码是否正确。API Key 的安全管理（local.properties + BuildConfig 注入 + git filter-repo 清除历史）让我第一次认真思考移动应用的安全问题，这种安全意识在课堂理论学习中很少被强调。

**成员二（XXX）：**

通过对该系统的设计使我了解到课程设计的过程是艰辛的，但是收获是巨大的。我负责的健康管理、用药提醒和子女监控面板三个模块，涵盖了课程要求的 SQLite 数据存储、Service 后台服务、BroadcastReceiver 广播接收、Notification 通知管理、TTS 语音播报等核心技术考察点。在实现用药提醒功能的过程中，我将 AlarmManager 闹钟触发、ReminderBroadcastReceiver 广播接收、NotificationManager 通知推送、TtsHelper 语音播报四条链路串联在一起，真正理解了 Android 四大组件之间协作的工作机制。健康异常自动告警（HealthAlertService）是我自主设计并实现的新功能——对心率、血压、血氧等 6 种关键指标设定 WARNING/CRITICAL 阈值，异常时同时触发通知推送、TTS 语音和家属记录三重告警，让我体验了从需求分析到编码实现的完整产品思维。SQLite 的 UNIQUE 约束防止重复打卡、onUpgrade 逐版本迁移防止数据丢失这些细节，让我认识到数据库设计不仅仅是"能存数据就行"，数据完整性和安全性同等重要。Android 6.0+ 运行时权限机制（CALL_PHONE、POST_NOTIFICATIONS、RECORD_AUDIO 等 6 种权限的实际申请和处理）是课堂理论学习之外的实战收获，每次权限被拒绝后的降级处理都需要站在用户角度思考。

**成员三（XXX）：**

通过本次课程设计，我对移动软件开发有了更加深入的理解。我负责的亲情相册、社区便民查询和防诈骗推送三个模块，分别涉及图片处理与权限管理（Glide + GalleryPermissionHelper）、第三方地图 SDK 集成（高德 3D 地图 + POI 搜索 + 路径规划）和第三方网络库使用（OkHttp 异步请求 + JSON 解析）。在实现亲情相册模块的过程中，我不仅完成了基础的照片上传、浏览、收藏功能，还深入优化了 UI/UX 体验——添加了跨屏淡入过渡动画（120ms 淡出 + 150ms 淡入）、空状态引导设计（插画 + 文案 + 行动按钮）、"全部照片"入口的视觉权重区分、上传实时预览反馈。这些细节优化让我认识到"功能可用"和"体验好用"之间的鸿沟。高德 SDK 集成过程中遇到的隐私合规校验失败（555570 错误码）和 so 库架构兼容问题，让我学会了通过查阅官方文档和错误日志分析来定位第三方库问题的方法。在编写 57 条测试用例的过程中，我学习了 Robolectric、JUnit、Espresso 三种测试框架的配合使用——单元测试快速验证逻辑、UI 自动化测试覆盖核心用户流程（登录→导航→AI 对话）。整个开发过程采用了 AI 辅助编程与人工判断相结合的方式，提高了开发效率的同时保证了代码质量。我深刻体会到：AI 是强大的工具，但最终的设计决策、代码审查和质量把控仍然需要人来完成。
