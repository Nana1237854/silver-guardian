# 银发守护者 (Silver Guardian) — 完整开发指令

## 项目概况

这是一个面向老年人的智能陪伴 Android 原生 App。SQLite 本地存储，智谱 GLM-4 驱动 AI 对话，高德 SDK 驱动地图与导航。当前代码存在大量硬编码数据，且两个预设用户（颜爷爷、林奶奶）共用同一套数据。

**核心目标：** 将所有硬编码改为传入数据，每个用户登录后看到完全属于自己的那份数据，并实现下列 12 项功能。

---

## 领域术语表

开发过程中请遵循以下命名和概念。Avoid 后的词不要用。

**老人 (Elder)**: 应用的主要使用者，通过 PIN 码登录。每个老人拥有完全独立的数据上下文。Avoid: 用户、patient

**家属 (Family Member)**: 与老人关联的子女或其他亲属，可在子女模式中查看老人数据并远程管理。Avoid: 监护人、子女

**子女模式 (Child Mode)**: 家属专用的监控与管理面板，可查看老人健康/用药/相册数据，远程添加用药提醒、查看老人位置。Avoid: 家属端、监控模式

**健康档案 (Health Record)**: 老人的健康指标记录，支持 13 种指标类型（血压、心率、血氧、睡眠、步数、血糖、体温、体重、体脂率、呼吸率、运动、心情、呼吸正念）。Avoid: 健康数据、体检记录

**今日健康概览 (Today's Health Overview)**: 健康档案首页摘要视图，展示当天最新 4 项核心指标（血压、心率、血氧、睡眠）及 AI 状态评估。Avoid: 健康摘要、概览卡片

**用药提醒 (Medicine Reminder)**: 按时间计划提醒老人服药的机制，包含药品信息、服用时间、打卡记录。触发时通过通知栏 + TTS 语音播报。Avoid: 吃药提醒、闹钟

**服药打卡 (Medicine Check-in)**: 老人确认已服药的每日签到记录。Avoid: 签到、确认

**药品库 (Medicine Library)**: 药品参考目录，按疾病分类。每个老人拥有一份独立拷贝，AI 推荐的新药可反向扩充入库。老人浏览，家属管理（CRUD）。Avoid: 药品目录、药典

**智能对话 (AI Chat)**: 智谱 GLM-4 模型驱动。支持语音输入和 AI 回复自动播报。Avoid: 聊天、消息

**亲情相册 (Family Album)**: 家属为老人上传的照片集合，支持拍照和相册选取。Avoid: 照片墙、图库

**一键呼叫 (One-Tap Call)**: 首页按钮，弹出大按钮快捷选择家属后直拨，不含 120。Avoid: 直接拨号

**SOS 紧急呼叫 (SOS Emergency)**: 首页红色按钮，确认弹窗 + 10 秒超时自动拨打 + 逐个拨打。课设展示 120 用空号。Avoid: 报警、求救

**便民查询 (Community POI Search)**: 高德 SDK 搜索附近菜市场、医院、药店、超市、银行，支持步行路线绘制和导航跳转。Avoid: 周边查询

**语音播报 (TTS Voice Broadcast)**: 用药提醒和健康异常告警的通知栏 + TTS 朗读。Avoid: 语音提醒

**长辈模式 (Elder Mode)**: 与字体设置联动，标准=完整布局，加大=精简，特大=极简 4 按钮。Avoid: 大字模式、关怀模式

**每日关怀 (Daily Care Notification)**: 早 8 点推送，含天气和服药计划汇总。Avoid: 早安推送

**天气出行建议 (Weather & Outing Advice)**: 首页天气卡片 + 结合 POI 的出行建议。Avoid: 天气预报

**PIN 提示 (PIN Hint)**: 创建时可填提示问答，登录页支持显示/隐藏 PIN 和忘记 PIN 验证。Avoid: 密码提示

**默认模板 (Default Template)**: 新用户自动拷贝的初始数据集，含 8 种常见药品和 4 条防骗贴士。Avoid: 初始数据、种子数据

---

## 需求 1：多用户数据隔离（最先做，是所有需求的基础）

这个 App 有两个预设用户：颜爷爷和林奶奶。现在的问题是，所有数据——健康记录、药品、家属、相册、记忆、甚至界面上"早上好，颜爷爷"——全是硬编码的，而且两个用户共用同一套数据，分不开。

我要你把数据层改成真正的多用户隔离：每个用户登录后，看到的是完全属于他自己的那份数据。具体来说：

**底层约束**
- 所有带 `user_id` 的表，seed 和读写都以当前登录用户为基准，不再默认查 user_id=1
- MedicineLibrary、FraudTips 现在是在 Repository 里 static 共享的，也要改成人各一份。新建用户时，从默认模板（8种常见药品：硝苯地平、缬沙坦、二甲双胍、阿司匹林、碳酸钙 D3、对乙酰氨基酚、氨溴索、奥美拉唑 + 4条防骗贴士）拷贝一份初始数据给他

**UI 约束**
- 所有现在写死"颜爷爷"的字符串（问候语、hint、头像描述等），一律改成从当前用户的 name 字段动态取

**新建用户约束**
- 创建新用户时，自动为该用户跑一遍所有 seed，拿到一套初始空白数据 + 默认模板

**现有用户处理**
- 颜爷爷和林奶奶的旧数据全部清空，和新建用户一样，走默认模板重新初始化，不再保留任何硬编码内容

不要改 UI 布局和交互行为，只改数据层和字符串取值方式。

---

## 需求 2：健康档案硬编码动态化

HealthFragment 当前所有数据都是写死的——4 个指标卡片（血压 128/76、心率 72、血氧 98%、睡眠 7h20m）从没读过数据库，顶部横幅"您好，颜爷爷"也是字符串资源写死。

要改成：

- **4 个概览卡片**（血压、心率、血氧、睡眠）改为从 SQLite 读取当前用户当天的该指标最新一条记录。不再写死值
- **数据来源优先级**：外部实时数据（蓝牙设备回调 + AI 对话解析的指标）优先更新 → 落库后从 SQLite 读取展示
- **今日严格过滤**：`created_at` 在今天的记录才展示在概览卡片上
- **下方"更多记录"列表**：使用现有的 `item_health_card.xml`（该布局文件存在但从未被引用），用 RecyclerView 展示当天所有指标记录
- **顶部横幅**：用户名从 `User.name` 动态取值。状态文字用简单规则：遍历今日所有指标，全部正常 → 显示"今天状态平稳"，任一异常 → 显示"部分指标异常，请注意"
- **不要在 XML 里写死任何指标值或用户名**

---

## 需求 3：用药提醒与 AI 对话双向同步

当前 AI 的 system_prompt.txt 里有一份独立维护的药品推荐表，App 的 MedicineLibrary 有另一份。两者零连接。AI 说的药不会进用户药品列表，用户当前在吃什么药 AI 也不知道。

要改成：

- **用户→AI 方向**：构造 AI 请求时，将当前用户的用药列表（药品名称、类型、服用时间）和当日服药打卡状态（哪些已吃、哪些未吃）作为 system prompt 的一部分发给 AI。这样 AI 能说"你已经在吃硝苯地平了，注意别和 XX 重复"或"你今天硝苯地平还没吃"
- **AI→用户方向**：AI 推荐药物时要求输出结构化 JSON 格式 `{"drugs":[{"name":"...","type":"...","usage":"...","description":"..."}]}`。App 解析后弹出确认对话框"AI 推荐了以下药品，是否加入用药列表？"，用户确认后写入该老人的药品库和用药提醒
- **双重兜底**：改 system_prompt.txt 要求 JSON 格式输出 + App 端正则提取药名作为备选解析
- **药库扩充**：AI 推荐了药品库中没有的药 → 自动加入该老人的药品库（去重）
- **System Prompt 动态化**：`system_prompt.txt` 中的药品推荐表不再硬编码，改为运行时从 MedicineLibrary 动态拼入

---

## 需求 4：家人相册上传与删除

现有上传代码有两个 Bug：AlbumFragment 设置图片 URL 只在内存对象上，没写回 SQLite（重启就丢）；ChildModeActivity 更是根本没设置 URL。删除只能整个相册删，不能单张删。

要改成：

- **上传流程重写**：点击上传 → 先选图片来源（拍照/相册）→ 图片选择完成预览 → 填标题和留言 → 点击保存 → 一次性写入 SQLite（包含正确的图片 URI）。不再"先建空记录再补 URL"
- **添加 CAMERA 权限**：AndroidManifest 加 `<uses-permission android:name="android.permission.CAMERA" />`，支持 `Intent.ACTION_IMAGE_CAPTURE` 现场拍照
- **添加单张照片删除**：在照片详情页或长按照片弹出删除选项，调用 `AlbumDao.delete(photoId)`。保留现有整相册删除逻辑
- **图片双存**：拍照或选图后，将图片同时保存到 App 私有目录（`context.getFilesDir()/photos/`）和外部 Pictures 文件夹（`Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)/SilverGuardian/`）
- **修复 ChildModeActivity 上传**：子女模式上传照片也要正确设置 URL 并持久化

---

## 需求 5：登录 PIN 提示

当前 PIN 输入框没有任何提示机制。老人忘记 PIN 就进不去。

要改成：

- **PIN 显示/隐藏切换**：PIN 输入框右侧加一个小眼睛图标（ImageView），点击切换 `inputType` 在 `numberPassword` 和 `number` 之间
- **新建用户添加 PIN 提示**：创建用户对话框中增加可选字段——"提示问题"（EditText，如"我的生日是几号？"）和"提示答案"（EditText）。存入 User 表（新增 `hint_question` 和 `hint_answer` 列）
- **忘记 PIN 流程**：登录页 PIN 输入区域下方加一个"忘记 PIN？"文字按钮 → 点击弹窗显示该用户的提示问题 → 用户输入答案 → 与存储的 `hint_answer` 比对 → 匹配则弹窗显示 PIN 码

---

## 需求 6：药品库功能完善

当前药品库是 Repository 里一个 static 的 ArrayList，只在内存里，没有持久化，也没有独立浏览页面。搜索是简单子串匹配。

要改成：

- **SQLite 持久化**：新增 `medicine_library` 表（`id, user_id, disease, medicine_name, brand, type, description`），首次创建用户时从默认模板 INSERT 8 条记录
- **独立浏览页面**：在 MedicineFragment 的"药品库"按钮点击后跳转到一个新的 Activity 或 DialogFragment，按疾病分类展示所有药品（用 ExpandableListView 或分组 RecyclerView）
- **搜索增强**：搜索框支持按药品名、疾病名、品牌名模糊搜索
- **权限模型**：
  - 老人（从 MainActivity 进入）：只能浏览和搜索，不能编辑
  - 家属（从 ChildModeActivity 进入）：可新增、编辑、删除药品库条目
- **AI 自动入库**：需求 3 中 AI 推荐的药品，确认后自动写入该老人的 medicine_library 表（同名去重）

---

## 需求 7：健康档案手动记录

当前"手动记录"按钮永远只插入 `heart_rate=69, 状态=正常`，没有任何表单让用户选择指标类型或输入数值。

要改成：

- 点击"手动记录"→ 弹出 AlertDialog
- **第一步**：下拉选择指标类型（Spinner），列出全部 13 种：心率、血压、血氧、睡眠、步数、血糖、体温、体重、体脂率、呼吸率、运动、心情、呼吸正念
- **第二步**：根据选择的指标类型，动态切换输入控件：
  - 血压：两个 EditText（收缩压 + 舒张压），单位 mmHg
  - 心率/步数/体温/体重/体脂率/呼吸率：一个 EditText + 单位标签
  - 血氧：一个 EditText + % 单位
  - 睡眠：小时 EditText + 分钟 EditText
  - 运动：分钟 EditText
  - 心情：横向表情符号选择器（😊😐😟😢），映射到"良好/一般/低落/很差"
  - 呼吸正念：分钟 EditText
  - 血糖：一个 EditText + mmol/L 单位
- **第三步**：可选备注 EditText
- **保存**：调用 `HealthDao.add(userId, type, value, status)` → 刷新概览卡片和更多记录列表 → Toast 提示
- 记录插入后同步触发 `HealthAlertService.checkAndAlert()` 做异常检测

---

## 需求 8：便民查询导航功能

当前 POI 搜索后，步行路线只以纯文本弹窗展示（距离、时间、步数），用户看不到地图上的路线轨迹。"打开高德导航"按钮也没有弹窗提示（直接跳转或静默失败）。

要改成：

- **步行路线绘制**：点击地图上的 POI 标记 → 调用高德 `RouteSearch.calculateWalkRouteAsyn()` → 拿到 `WalkPath` → 用 `WalkRouteOverlay` 在地图上绘制蓝色步行路线（替换现有纯文本弹窗）
- **POI 信息栏**：在地图下方、类别筛选按钮下方加一个 `LinearLayout` 作为信息栏。点击 POI 后显示：POI 名称 + 距离（如"350m"）+ 步行时间（如"约 4 分钟"）。默认隐藏，无选中 POI 时 GONE
- **"开始导航"按钮**：信息栏右侧加一个"开始导航"按钮。点击逻辑：
  1. 尝试打开高德 App（`Intent` 跳到 `com.autonavi.minimap`）
  2. 如果高德 App 未安装 → 弹 AlertDialog："未安装高德地图，是否使用网页版导航？"→ 确认后用 `https://uri.amap.com/navigation` 在浏览器打开
- 保留现有 5 类 POI 搜索芯片和功能不变

---

## 需求 9：一键呼叫与 SOS 紧急呼叫

MainActivity 里 `oneTapCall()` 和 `oneTapCallLongPress()` 代码写好了，但没有任何按钮调用它们。`bg_button_sos.xml`、`ic_call.xml`、`sos_button` 字符串全都有，但从没被用过。

要改成：

**一键呼叫（首页绿色按钮）：**
- 在 HomeFragment 的首页布局中（`fragment_home.xml` 的快捷方式区域上方或旁边），添加一个绿色按钮，文字"一键呼叫"，图标 `ic_call.xml`
- 点击 → 弹出 AlertDialog，用大按钮（不是列表）展示所有家属：按钮文字为家属姓名（如"大明"），每个按钮高度至少 60dp
- 点击某个家属 → 调用 `Intent.ACTION_CALL` 直拨该家属电话（`CALL_PHONE` 权限已有）
- 不包含 120

**SOS 紧急呼叫（首页红色按钮）：**
- 与一键呼叫并排，红色按钮，使用 `bg_button_sos.xml` 作为背景，文字 "SOS 紧急呼叫"
- 点击 → 弹出 AlertDialog："是否紧急呼叫？" + 10 秒倒计时显示（TextView 实时更新秒数）
- 10 秒内用户未做任何操作 → 自动触发拨打 120 测试号（课设展示使用空号 "00000000000" 或 `tel:10086`）
- 用户主动点击"确认呼叫"→ 弹出联系人选择列表（120 测试号在最上面 + 所有家属），选中后逐个拨打
- 拨打逻辑：调用 `makePhoneCall(phone)`，如果电话未接通（无法检测，简单轮询），继续打下一个

**注意**：调用 120 时使用测试号而非真实 120，避免误拨急救中心。

两个按钮相互独立，并排在首页显眼位置。

---

## 需求 10：用药提醒通知栏 + 语音播报

当前有一个致命 Bug：`ReminderBroadcastReceiver` 的闹钟只在 `BootReceiver`（开机时）设置。用户平时加药根本不会触发提醒。通知栏推送和 TTS 语音播报的代码是写好的，但闹钟没设上。

要改成：

- **加药时立即设闹钟**：`Repository.addMedicine()` 或 `MedicineDao.add()` 完成后，立即调用一个 `scheduleMedicineAlarm(context, userId, medicine)` 方法，用 `AlarmManager.setRepeating(RTC_WAKEUP, triggerTime, INTERVAL_DAY, pendingIntent)` 设定每日闹钟
- **删药时取消闹钟**：`Repository.deleteMedicine()` 时，调用 `AlarmManager.cancel(pendingIntent)` 取消对应闹钟
- **精确闹钟权限**：AndroidManifest 加 `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`。在设置闹钟前检查权限，未授权则引导用户开启
- **自定义提醒配置**：在添加药品对话框加两个可选设置：
  - "提前提醒"（Spinner：准时/提前 5 分钟/提前 10 分钟/提前 15 分钟）
  - 存到 `user_medicines` 表新增列 `advance_minutes`
- **重复提醒**：如果到了服药时间但 `takenToday=false`（用户没打卡），按配置重复提醒：
  - 在加药时可选"重复提醒次数"（0-5 次）和"重复间隔"（5/10/15/30 分钟）
  - 存到 `user_medicines` 表新增列 `repeat_count` 和 `repeat_interval`
  - 闹钟触发时检查 `takenToday`，如果 false 且 repeat_count > 0，用 `AlarmManager.set()` 设置一次性后续闹钟
- 保留现有 `ReminderBroadcastReceiver` 通知栏推送（`NotificationCompat.Builder` + channel `medicine_reminder`）+ `TtsHelper.speak()` 语音播报不变
- 修复 `EmergencyDao.readAll()` 的 Bug：time 和 status 改为从游标实际列读取，不要硬编码"今天"和"已响应"

---

## 需求 11：长辈模式（字体联动首页极简布局）

现有设置页的字体切换（标准/加大/特大）只改字号，不改布局。要把它改成联动首页布局复杂度。

- **标准（normal, 1.0x）**：首页保持现有完整布局（欢迎横幅 + 健康概览 + 快捷方式 + AI 卡片）
- **加大（large, 1.2x）**：首页改为精简版——大按钮快捷网格（健康、用药、相册、便民）+ 下方今日用药提醒汇总卡片。创建新布局文件 `fragment_home_simplified.xml`
- **特大（xlarge, 1.4x）**：首页改为极简版——仅 4 个大按钮纵向排列（SOS 紧急呼叫、一键呼叫、用药提醒、AI 对话），按钮高度至少 72dp，间距 20dp，文字超大。创建 `fragment_home_minimal.xml`
- **实现方式**：`FontScaleHelper` 新增 `public static boolean isSimplifiedMode(Context)` 和 `public static boolean isMinimalMode(Context)` 两个方法。`HomeFragment.onCreateView()` 根据这两个方法决定 inflate 哪个布局文件
- **不新增设置入口**，直接替换现有字体 Spinner 的行为
- 切换字体时 `requireActivity().recreate()` 保持不变，新布局随 Activity 重建自动生效

---

## 需求 12：额外老人功能

**AI 语音对话增强：**
- ChatDetailActivity 输入框区域加一个麦克风按钮，长按开始录音，松开结束
- 使用 `android.speech.SpeechRecognizer` 进行语音识别，识别结果填入输入框并自动发送
- AI 回复到达后，自动调用 `TtsHelper.speak(reply)` 朗读（当前已有部分实现，确保所有回复都朗读，检查 `aiChat().appendAiReply()` 中的 TTS 调用是否缺漏）

**每日关怀通知：**
- 新增 `DailyCareReceiver`（BroadcastReceiver），注册到 AndroidManifest
- 在 `BootReceiver` 中加一段：设定每天早上 8:00 的 `AlarmManager` 闹钟（`DAILY_CARE` action），指向 `DailyCareReceiver`
- `DailyCareReceiver.onReceive()` 中：
  1. 获取当天天气（通过免费天气 API，如 wttr.in 或和风天气）
  2. 查询当前用户的当日用药计划（名称 + 时间列表）
  3. 构建通知内容："早上好！今天天气：晴，18-25°C。今日用药：硝苯地平(08:00)、阿托伐他汀(20:00)。祝您健康愉快！"
  4. 发送高优先级通知（channel `daily_care`）+ TTS 朗读

**子女端增强（ChildModeActivity）：**
- 添加"一键回拨"按钮：点击后调用 `Intent.ACTION_CALL` 拨打当前老人的第一个家属电话
- 添加"添加用药提醒"入口：点击弹出同样的添加药品对话框（同 MedicineFragment），写入当前老人数据
- 添加"查看老人位置"卡片：获取当前 GPS 位置（复用高德定位），显示文字"老人当前位置：XX 路 XX 号附近" + 时间戳。供演示用，不要求实时追踪

**天气卡片 + 出行建议：**
- **定位**：复用高德 SDK `AMapLocationClient`（`CommunityActivity` 已有），获取当前 GPS 坐标 → 逆地理编码（`GeocodeSearch`）得到城市名 + 区县名
- **天气查询**：用城市名调 和风天气免费版 API（注册免费 Key，支持天气类型 + 温度 + AQI）。在 `Repository` 或新建 `WeatherModule` 中封装网络请求
- **判断规则（三项综合）**：
  - 天气类型：雨/雪/大风/台风 → 不建议出门
  - 温度：>35°C 或 <5°C → 不建议出门
  - 空气质量：AQI > 150 → 不建议出门
  - 三项全通过 → 适合出门
- **POI 推荐（调高德 SDK）**：
  - 搜当前位置附近"公园"（`PoiSearch.Query("公园")`，半径 5000m）→ 取最近的前 3 个
  - 没公园 → 降级搜"广场" → 再没就搜"社区活动中心"
  - 不管适不适合出门，同时搜最近"医院"
  - 全搜不到就只显示天气，不出地点建议
- **首页卡片文案示例**：
  - 适合出门：`"广州·天河区 晴天 22°C | 空气优 AQI 42\n今天适合出门。推荐：天河公园，距您 1.2km。如需就医，中山三院距您 800m。"`
  - 不适合出门：`"广州·天河区 暴雨 15°C | AQI 165 中度污染\n建议在家活动。如需就医，中山三院距您 800m。"`
- **推送联动**：每日关怀（`DailyCareReceiver` 早 8 点）附带当日天气 + 出行建议摘要
- 首页（完整布局模式）添加天气卡片，位置在 AI 卡片下方，创建 `view_weather_card.xml` 布局文件

---

## 实现顺序建议

1. **先做需求 1**：多用户数据隔离是所有需求的基础，数据库结构改完再往下走
2. **需求 2 + 6 + 7**：健康档案动态化 + 药品库 + 手动记录，都是数据层改造
3. **需求 3**：AI 双向同步，依赖药品库和用药提醒已有
4. **需求 4 + 5**：相册和 PIN 提示，相对独立
5. **需求 8 + 9**：便民查询导航 + 一键呼叫/SOS，可并行
6. **需求 10 + 11 + 12**：用药提醒修复 + 长辈模式 + 额外功能，最后打磨

---

## 关键约束

- 不要改 UI 布局和交互行为（除非需求明确要求改，如长辈模式）
- 新建布局文件可以，修改现有 XML 也可以，但不能破坏不相关的页面
- 120 号码在课设版本统一使用测试号 `10086`（中国移动客服），不要用真实 120
- 颜爷爷和林奶奶的种子数据要清理，改用默认模板初始化
- 所有新增的数据库列需要用 `ALTER TABLE` 或在 `ElderlyDbHelper.onCreate()` 中更新 schema
