# UI 微调定位文档

## 1. 文档目的

这份文档用于帮助你快速定位项目里“按钮文字大小、图标位置、板块顺序与间距、卡片圆角、页面留白”等排版与布局相关代码。

这个项目的 UI 来源分成三类：

1. XML 静态布局。
2. Java 动态拼装页面。
3. 全局样式令牌与 drawable 背景。

因此，微调时通常要同时看：

- 页面入口类：`Activity` / `Fragment`
- 复用组件：`item_*.xml`
- 全局令牌：`values/dimens.xml`、`values/colors.xml`、`values/themes.xml`
- 背景与按钮形状：`res/drawable/*.xml`

---

## 2. 全局优先修改入口

如果你想先统一界面气质，而不是逐页手改，优先看下面这些文件。

### 2.1 尺寸与字号令牌

- `app/src/main/res/values/dimens.xml`

核心参数：

- `text_display = 32sp`
- `text_title = 26sp`
- `text_subtitle = 21sp`
- `text_body = 17sp`
- `text_small = 14sp`
- `button_height = 56dp`
- `icon_size_small = 24dp`
- `icon_size_medium = 30dp`
- `screen_padding = 18dp`
- `card_padding = 18dp`
- `section_gap = 16dp`
- `button_radius = 28dp`
- `card_radius = 22dp`
- `bottom_nav_height = 82dp`

建议：

- 想让整体更精致：先把 `screen_padding` 从 `18dp` 调到 `20dp`。
- 想让按钮更轻盈：把 `button_height` 从 `56dp` 调到 `52dp`，同时把按钮文字从 `17sp/18sp` 降到 `16sp/17sp`。
- 想让卡片没那么“圆”：把 `button_radius` 从 `28dp` 调到 `24dp`，`card_radius` 从 `22dp` 调到 `18dp`。

### 2.2 全局主题与按钮文字

- `app/src/main/res/values/themes.xml`

关键点：

- `SilverGuardianButton`
- `BottomNavText`

建议：

- 底部导航字太小可把 `BottomNavText` 从 `12sp` 调到 `13sp`。
- Material Button 默认文字来自 `SilverGuardianButton`，如果要统一按钮字号，可先改这里。

### 2.3 全局颜色

- `app/src/main/res/values/colors.xml`

常用微调色：

- 主绿：`primary` / `primary_dark`
- 页面底色：`bg_page`
- 卡片文字：`text_primary` / `text_secondary`
- 分割线：`divider`
- 底部导航：`nav_item_active` / `nav_item_inactive` / `bottom_nav_active`

建议：

- 如果界面想更干净，可以把 `bg_page` 稍微调白一点。
- 如果辅助文字太虚，可把 `text_secondary` 再加深一点。

### 2.4 通用按钮与卡片背景

- `app/src/main/res/drawable/bg_button_primary.xml`
- `app/src/main/res/drawable/bg_button_secondary.xml`
- `app/src/main/res/drawable/bg_group_surface.xml`
- `app/src/main/res/drawable/bg_chip_soft.xml`

建议：

- 主按钮太重：保持颜色不变，把圆角降到 `24dp`。
- 次按钮边框太抢眼：把 `bg_button_secondary.xml` 的 `stroke` 从 `@color/primary` 改成 `@color/divider` 或更深一点的中性色。
- 分组卡片太臃肿：把 `bg_group_surface.xml` 圆角从 `22dp` 改到 `18dp`。

### 2.5 动态表单统一入口

- `app/src/main/java/com/silverguardian/prototype/utils/FormFieldFactory.java`

影响范围：

- 搜索弹窗
- 创建相册弹窗
- 上传照片弹窗
- 记忆新增弹窗
- 其他程序化表单

建议：

- 表单标题标签字号：`15sp -> 14sp`
- 输入框字号：`17sp -> 16sp`
- 输入框最小高度：单行 `56dp -> 52dp`，多行 `96dp -> 88dp`

---

## 3. 页面总览

### 3.1 主框架页

- `MainActivity`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`

负责：

- 底部导航
- 主内容容器
- Home / Health / Medicine / Album / Settings 五个主页面切换

建议优先微调：

- `BottomNavigationView` 高度、边距、图标尺寸
- 主内容与底部导航的安全间距

### 3.2 主页面列表

- 登录页：`LoginActivity` + `activity_login.xml`
- 首页：`HomeFragment` + `fragment_home.xml`
- 健康档案：`HealthFragment` + `fragment_health.xml`
- 用药提醒：`MedicineFragment` + `fragment_medicine.xml`
- 家人相册：`AlbumFragment` + `item_album.xml` + `item_photo_grid.xml`
- 设置：`SettingsFragment`

### 3.3 次级页面列表

- 记忆回忆：`MemoryFragment`
- 防诈提醒：`FraudFragment`
- 便民查询简版：`CommunityFragment`
- 便民查询地图版：`CommunityActivity`
- 聊天详情：`ChatDetailActivity` + `activity_chat_detail.xml`
- 照片详情：`PhotoDetailActivity`
- 子女模式：`ChildModeActivity`
- 蓝牙设备：`BluetoothActivity`
- 防诈详情：`FraudDetailActivity`

---

## 4. 各页面定位与微调建议

## 4.1 登录页

入口：

- `app/src/main/java/com/silverguardian/prototype/LoginActivity.java`
- `app/src/main/res/layout/activity_login.xml`
- 用户卡片：`app/src/main/res/layout/item_user_avatar.xml`

主要板块：

1. 顶部吉祥物图 + 标题 + 副标题
2. 用户选择区 `user_grid`
3. 当前选中用户名 `selected_user_name`
4. “添加老人”按钮 `add_user_button`
5. PIN 卡片
6. 登录按钮 `login_button`

建议微调点：

- 标题与副标题间距：`activity_login.xml` 中 `12dp`、`5dp` 可微调为 `10dp`、`4dp`。
- 用户卡片头像尺寸：`item_user_avatar.xml` 中 `64dp` 可改为 `60dp`。
- 用户名字号：`16sp` 可改为 `15sp`，视觉更稳。
- PIN 输入框：高度 `64dp` 可降到 `58dp`，字重保留，界面会更轻。
- 登录按钮：`58dp` 可统一成全局 `56dp` 或 `52dp`。

## 4.2 主框架与底部导航

入口：

- `app/src/main/java/com/silverguardian/prototype/MainActivity.java`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`
- `app/src/main/res/values/themes.xml`

主要板块：

1. `fragment_container`
2. `bottom_navigation`
3. 五个底部 tab 图标与文字

建议微调点：

- 导航整体高度：`82dp -> 78dp`
- 左右边距：`12dp -> 16dp`
- 底边距：`10dp -> 12dp`
- 图标尺寸：`27dp -> 24dp` 或 `25dp`
- 文案字号：`12sp -> 13sp`

如果你觉得底部导航“浮得太高”：

- 减少 `layout_marginBottom`
- 或减弱 `bg_bottom_nav` 的阴影感

## 4.3 首页 Home

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/HomeFragment.java`
- `app/src/main/res/layout/fragment_home.xml`

主要板块：

1. 顶部 hero 图
2. 健康概览卡
3. 四个功能入口行
4. AI 助手大卡片

建议微调点：

- Hero 高度：`205dp` 可改 `188dp` 或 `196dp`
- Hero 左侧文字区域宽度：`230dp` 可调窄一点，避免过满
- 健康概览卡上移量：`layout_marginTop="-12dp"` 可改 `-8dp`，减少压叠感
- 功能入口行高度：`82dp -> 76dp`
- 左侧功能图标：`46dp` 外框 + `10dp` padding，可改成 `44dp` + `9dp`
- 箭头字符区域 `48dp` 可以缩成 `40dp`

## 4.4 健康档案页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/HealthFragment.java`
- `app/src/main/res/layout/fragment_health.xml`
- 卡片项：`app/src/main/res/layout/item_health_card.xml`

主要板块：

1. 页面标题与“今日状态”标签
2. 筛选 Spinner
3. 蓝牙按钮
4. 添加按钮
5. 健康记录列表

建议微调点：

- 顶部区域下边距可以再收紧，减少标题区占高。
- 蓝牙/添加按钮目前 `92dp x 52dp`，若感觉横向太挤，可改成 `84dp x 48dp`。
- `item_health_card.xml` 有明显侧边强调条，当前是 `8dp` 宽，这会偏重。

建议重点改：

- 侧边强调条：`8dp -> 4dp`
- 卡片圆角：`16dp -> 14dp`
- 卡片主值字号：`24sp -> 22sp`
- 状态胶囊 padding：缩小 1 到 2dp

## 4.5 用药提醒页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/MedicineFragment.java`
- `app/src/main/res/layout/fragment_medicine.xml`
- 卡片项：`app/src/main/res/layout/item_medicine.xml`

主要板块：

1. 标题
2. “添加药品提醒”按钮
3. 今日打卡进度 + 筛选器
4. 顶部提醒条
5. 药品卡片列表

建议微调点：

- 添加按钮：`56dp` 高可以保留，但文字 `18sp` 可降到 `17sp`
- 按钮图标间距：`drawablePadding="10dp"` 可改 `8dp`
- 筛选器宽度：`150dp` 可根据实际文案改 `136dp`
- 顶部提醒条 `minHeight="62dp"` 可改 `56dp`
- 药品卡片 `minHeight="150dp"` 可改到 `136dp`
- 类型 chip 文字 `12sp` 可升到 `13sp`，更易读

## 4.6 家人相册页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/AlbumFragment.java`
- 相册卡片：`app/src/main/res/layout/item_album.xml`
- 照片格子：`app/src/main/res/layout/item_photo_grid.xml`

这是当前最关键的微调页，且大多数结构都在 `AlbumFragment.java` 里动态生成。

### 4.6.1 相册列表页

位置：

- `buildAlbumList()`
- `familyBanner()`
- `allPhotosEntry()`

主要板块：

1. 顶部标题区
2. 横幅图 `familyBanner()`
3. “全部照片”入口 `allPhotosEntry()`
4. “我的相册”标题区
5. 相册列表 `RecyclerView`
6. 创建新相册按钮

建议微调点：

- 页面根 padding：`18dp` 可改 `20dp`
- 顶部头像：`52dp -> 48dp`
- 横幅高度：`150dp -> 140dp`
- “全部照片”卡高度：`108dp -> 100dp`
- “全部照片”左文字区宽度：`170dp -> 156dp`
- 封面宽度：`152dp -> 146dp`

### 4.6.2 相册详情页

位置：

- `buildPhotoGrid(String album)`
- `albumHero()`
- `memoryNote()`
- `photoToolbar()`

主要板块：

1. 顶部“返回 / 标题 / 更多”
2. 相册封面 Hero
3. 回忆说明卡片
4. 全部照片标题行
5. 照片网格
6. 底部操作条：排序 / 搜索 / 上传照片

建议微调点：

- 顶部返回、更多按钮来自 `textButton()`，文字 `14sp` 可改 `15sp`，按钮最小宽度 `64dp` 可改 `72dp`。
- Hero 高度：`218dp -> 204dp`
- Hero 内文 padding：`18dp -> 16dp`
- Hero 标题字号：`26sp -> 24sp`
- Hero 副文案与标题间距：`5dp -> 4dp`

### 4.6.3 回忆说明卡片

位置：

- `memoryNote()`

主要参数：

- 卡片 padding：`(16, 14, 16, 12)`
- 底部外边距：`14dp`
- 标题：`17sp`
- 正文：`14sp`
- 正文下边距：`8dp`
- 行动区按钮高度：`48dp`

建议：

- 如果你想让“回忆说明”位置更靠近封面：
  - 把 `albumHero()` 的 `bottomMargin` 从 `12dp` 降到 `8dp`
  - 把 `memoryNote()` 的 `bottomMargin` 从 `14dp` 调到 `10dp`
- 如果你想让这块更像“说明而不是主卡片”：
  - 标题 `17sp -> 16sp`
  - 正文 `14sp -> 15sp`
  - 卡片 padding 改成 `16, 12, 16, 10`

### 4.6.4 上传照片按钮

位置：

- `photoToolbar()`
- `primaryButton(String label, int iconRes)`
- 图标资源：`app/src/main/res/drawable/ic_upload.xml`
- 按钮背景：`app/src/main/res/drawable/bg_button_primary.xml`

当前实现特点：

- 按钮文本和图标是 `TextView + compound drawable`
- 图标在左，按钮整体 `gravity = center`
- 图标与文字间距来自 `setCompoundDrawablePadding(dp(8))`
- 按钮宽高目前是 `142dp x 56dp`

如果你想微调“上传照片”图标位置，优先改这里：

1. `uploadParams.width`
2. `primaryButton()` 里的 `setCompoundDrawablePadding()`
3. `primaryButton()` 里的 `setGravity()`
4. `primaryButton()` 里的左右 padding

建议参数：

- 按钮宽度：`142dp -> 150dp`
- 图标与文字间距：`8dp -> 6dp`
- 如果图标显得偏左，可增加按钮左内边距，或改成 `Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL`
- 如果想让图标和文字整体略靠左，建议使用：
  - `button.setGravity(Gravity.CENTER_VERTICAL);`
  - 再配合 `button.setPadding(dp(18), 0, dp(18), 0);`

### 4.6.5 照片网格卡片

位置：

- `item_photo_grid.xml`

主要参数：

- 卡片外边距：`4dp`
- 图片高度：`150dp`
- 标题内边距：`8 / 22 / 8 / 8dp`
- 标题字号：`14sp`
- 收藏标签边距：`6dp`
- 收藏标签字号：`14sp`

建议：

- 网格更轻：`layout_margin 4dp -> 6dp`
- 图片高度更匀称：`150dp -> 144dp`
- 底部标题带不那么厚：`paddingTop 22dp -> 18dp`
- “已收藏”标签更精致：`14sp -> 13sp`，`layout_margin 6dp -> 8dp`

## 4.7 设置页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/SettingsFragment.java`

主要板块：

1. 顶部个人卡片
2. 家人与设备
3. 安全与回忆
4. 显示与辅助
5. 其他

建议微调点：

- 分组标题与卡片间距可适当缩小，减少页面过长感。
- 行图标盒子当前多为 `52dp`，可统一到 `48dp`。
- 个人卡片头像 `76dp -> 72dp`
- 右侧箭头容器 `48dp -> 40dp`
- 字体大小偏多级混用，建议统一：
  - 分组标题 `18sp`
  - 描述 `14sp`

## 4.8 记忆回忆页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/MemoryFragment.java`

主要板块：

1. 标题
2. “+ 新增”按钮
3. 分类筛选 Spinner
4. 记忆列表

建议微调点：

- “+ 新增”按钮如果显得抢眼，可把文本从 `+ 新增` 改成 `新增回忆`，同时减小视觉噪音。
- 顶部按钮高度保持 `48dp~52dp` 更合适。
- 列表项背景来源于 `bg_group_surface`，如果页面太厚重，可考虑改成轻边框或更小圆角。

## 4.9 防诈提醒页

入口：

- `app/src/main/java/com/silverguardian/prototype/fragments/FraudFragment.java`

主要板块：

1. 标题
2. 刷新 chip
3. 加载状态文案
4. 提醒列表

建议微调点：

- “刷新” chip 可以统一成和其他页面相同的按钮高度标准。
- 列表卡片如果信息密，可以增加标题与元信息间距。
- 如果希望更正式，减少 chip 的薄荷底色饱和度。

## 4.10 社区便民查询页

入口：

- 简版：`app/src/main/java/com/silverguardian/prototype/fragments/CommunityFragment.java`
- 地图版：`app/src/main/java/com/silverguardian/prototype/CommunityActivity.java`

主要板块：

1. 标题
2. 横向搜索 chips
3. 状态提示条
4. 结果区或地图区
5. 导航按钮 / 返回按钮

建议微调点：

- 搜索 chips 横向间距建议统一为 `8dp`
- 状态提示条 padding 可以略减，避免太高
- POI 卡片中的“导航”按钮可以改窄一点，避免挤压文字区

## 4.11 聊天详情页

入口：

- `app/src/main/java/com/silverguardian/prototype/ChatDetailActivity.java`
- `app/src/main/res/layout/activity_chat_detail.xml`
- 消息项：`app/src/main/res/layout/item_chat_message.xml`

主要板块：

1. 顶部工具栏
2. AI 身份介绍卡
3. 消息列表
4. 快捷提问 chips
5. 输入区
6. 抽屉侧栏

建议微调点：

- 顶部工具按钮：`50dp/48dp/52dp` 建议统一成 `48dp`
- 标题胶囊高度：`52dp -> 48dp`
- 输入框最小高度：`56dp -> 52dp`
- 发送按钮 `54dp`、语音按钮 `54dp` 可统一成 `52dp`
- 气泡文字 `18sp` 对老人友好，但若你想更精致，可保留 AI 18sp、用户 17sp

## 4.12 照片详情页

入口：

- `app/src/main/java/com/silverguardian/prototype/PhotoDetailActivity.java`

主要板块：

1. 顶部页码导航
2. 大图
3. 标题
4. 已收藏标签
5. 分类 / 场景 / 说明 / 家属留言
6. 返回相册按钮

建议微调点：

- 大图高度可从 `300dp` 微调到 `280dp`
- 信息行 label/value 的横向距离可适当加大
- 返回按钮若过宽，可改为内容宽度 + 居中

## 4.13 子女模式页

入口：

- `app/src/main/java/com/silverguardian/prototype/ChildModeActivity.java`

主要板块：

1. 标题与说明
2. 家庭横幅图
3. 健康摘要
4. 今日服药
5. SOS 记录
6. 家属联系
7. 为老人上传照片
8. 返回老人端

建议微调点：

- 分段标题过多时，可减少 sectionHeader 上下间距。
- “为老人上传照片”按钮可复用相册页同一套按钮策略，保证一致性。
- 卡片之间建议统一 `12dp` 或 `14dp` 的节奏。

## 4.14 蓝牙设备页

入口：

- `app/src/main/java/com/silverguardian/prototype/BluetoothActivity.java`

主要板块：

1. 标题
2. 状态提示条
3. 扫描按钮
4. 设备列表
5. 加载演示设备
6. 返回设置

建议微调点：

- 状态条可减轻背景色，避免压住主操作。
- “连接”按钮如果太像筛选 chip，可以考虑加轻边框，强调这是操作按钮。

## 4.15 防诈详情页

入口：

- `app/src/main/java/com/silverguardian/prototype/FraudDetailActivity.java`

主要板块：

1. 分类标签
2. 标题
3. 案例详情卡片
4. 防范措施列表

建议微调点：

- 分类标签若过于醒目，可减小 padding 或字号。
- 详情卡片内部段落可增加 `lineSpacingExtra`，更易读。

---

## 5. 图标与按钮位置统一修改规则

## 5.1 底部导航图标

位置：

- `activity_main.xml`
- `bottom_nav_menu.xml`

控制参数：

- `app:itemIconSize`
- `BottomNavText`
- `nav_item_tint`

## 5.2 行入口图标

常见页面：

- 首页
- 设置页
- 健康页

控制方式：

- XML 里的 `ImageView` 宽高
- `padding`
- 背景 `bg_chip_soft` / `bg_icon_*`

建议：

- 图标盒子建议统一成 `44dp~48dp`
- 图标真实尺寸建议统一成 `20dp~24dp`

## 5.3 主按钮左侧图标

常见页面：

- 相册上传
- 用药新增

控制方式：

- `drawableStart`
- `compoundDrawablePadding`
- 按钮 `gravity`
- 左右 `padding`

这是最适合做“图标位置微调”的入口。

## 5.4 对话框与表单字段

位置：

- `FormFieldFactory.java`

建议：

- 先统一表单字段，再单独微调页面弹窗，否则会出现同类弹窗风格不一致。

---

## 6. 推荐的第一轮微调顺序

如果你想用最少改动先看到明显提升，推荐按这个顺序来：

1. 改 `dimens.xml`
2. 改 `themes.xml`
3. 改 `bg_button_primary.xml`、`bg_button_secondary.xml`、`bg_group_surface.xml`
4. 改 `activity_main.xml` 的底部导航尺寸
5. 改 `AlbumFragment.java` 的 `albumHero()`、`memoryNote()`、`photoToolbar()`
6. 改 `item_photo_grid.xml`
7. 改 `fragment_home.xml`、`fragment_medicine.xml`
8. 最后再逐页修小间距

---

## 7. 相册页的直接改法速查

如果你这轮主要还是想先调相册页，最值得先试的是：

- 页面整体留白：`AlbumFragment.onCreateView()` 里的 `root.setPadding(dp(18), ...)`
- 顶部按钮文字大小：`textButton()`
- 相册标题大小：`albumHero()` 里的 `txt(currentAlbum, 26, true)`
- 回忆说明位置：`buildPhotoGrid()` 中 `root.addView(albumHero())`、`root.addView(memoryNote())` 的顺序和两个方法里的 `bottomMargin`
- 回忆说明卡内边距：`memoryNote()`
- 上传照片图标位置：`primaryButton()`
- 上传照片按钮宽度：`photoToolbar()`
- 照片网格标题条厚度：`item_photo_grid.xml`
- 收藏标签位置：`item_photo_grid.xml`

---

## 8. 后续建议

当前项目已经有一套全局令牌，但很多动态页面仍直接写死 `dp()` / `sp()`。

如果你后续会继续频繁微调，建议下一步做两件事：

1. 把 `AlbumFragment`、`SettingsFragment`、`MemoryFragment` 里常用的硬编码尺寸抽到 `dimens.xml`
2. 把主按钮、次按钮、chip、分组卡片做成更统一的组件规则

这样后面就能从“改很多页面”变成“改一两个 token”。
