# UI 布局微调定位指南

## 1. 文档用途

这份文档用于做视觉审计后的快速定位，帮助你判断：
- 某个页面的标题、按钮、图标、卡片、section 在哪里改
- 间距、字号、圆角、高度主要由哪些 token 控制
- 哪些结构已经统一到 XML，可直接微调布局而不需要再去 Java 里找稳定 UI 树

当前项目已经以“页面级 XML + 局部复用 layout + Java 只负责绑定/状态”为主，因此微调时建议优先看：
1. 页面入口 XML
2. 局部复用块 `view_*.xml` / `item_*.xml`
3. 全局 token：`dimens.xml` / `themes.xml` / `colors.xml`
4. 形状与背景：`drawable/*.xml`

## 2. 全局优先入口

### 2.1 尺寸与间距 token
入口：`app/src/main/res/values/dimens.xml`

高频 token：
- 页面留白：`screen_padding`、`page_top_padding_compact`、`page_bottom_padding_standalone`
- 通用分段：`section_gap`、`section_label_spacing_top`、`section_label_spacing_bottom`
- 分组标题：`group_label_spacing_top`、`group_label_spacing_bottom`、`group_label_inset_start`
- 按钮：`button_height`、`button_min_width`、`button_padding_horizontal`
- chip：`chip_padding_horizontal`、`chip_padding_vertical`、`chip_gap_horizontal`
- compact chip：`compact_chip_padding_horizontal`、`compact_chip_padding_vertical`
- 状态条：`status_strip_min_height`、`status_strip_padding_horizontal`、`status_strip_padding_vertical`
- section/card：`group_card_padding_compact`、`card_content_padding_compact`、`section_card_padding_horizontal`、`section_card_padding_top`、`section_card_padding_bottom`
- 设置行：`settings_row_*`
- 详情页：`detail_*`、`fraud_detail_*`

建议：
- 想整体更松一些，先从 `screen_padding`、`section_gap` 下手。
- 想统一“标题和内容的呼吸感”，优先改 `section_label_spacing_*` 和 `group_label_spacing_*`。
- 想统一卡片密度，优先改 `group_card_padding_compact` 与 `section_card_*`。

### 2.2 文字层级与按钮样式
入口：`app/src/main/res/values/themes.xml`

文字层级：
- `TextAppearance.SilverGuardian.PageTitle`
- `TextAppearance.SilverGuardian.SectionTitle`
- `TextAppearance.SilverGuardian.GroupLabel`
- `TextAppearance.SilverGuardian.Body`
- `TextAppearance.SilverGuardian.BodyMuted`
- `TextAppearance.SilverGuardian.DetailValue`

常用控件样式：
- `Widget.SilverGuardian.PrimaryTextAction`
- `Widget.SilverGuardian.SecondaryTextAction`
- `Widget.SilverGuardian.ChipText`
- `Widget.SilverGuardian.CompactChip`
- `Widget.SilverGuardian.CardActionText`
- `Widget.SilverGuardian.SettingsRowArrow`
- `Widget.SilverGuardian.SettingsRowStatus`

建议：
- 想统一主按钮的高度、字重、图标与文字关系，先看 `PrimaryTextAction`。
- 想统一次按钮或“返回设置/返回相册”这类按钮，先看 `SecondaryTextAction`。
- 想统一 section 标题和 group label 的层级感，优先改 text appearance，而不是逐页改 `TextView`。

### 2.3 颜色与背景
入口：
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/drawable/bg_button_primary.xml`
- `app/src/main/res/drawable/bg_button_secondary.xml`
- `app/src/main/res/drawable/bg_group_surface.xml`
- `app/src/main/res/drawable/bg_chip_soft.xml`
- `app/src/main/res/drawable/bg_reminder_strip.xml`

建议：
- 卡片厚重感主要来自 `bg_group_surface.xml`
- 主按钮观感主要来自 `bg_button_primary.xml`
- 轻量 chip / toggle / tag 主要来自 `bg_chip_soft.xml`
- 状态提示条主要来自 `bg_reminder_strip.xml`

## 3. 已统一的共享块

以下块已经是当前主线页面的统一入口，微调时优先改这里：
- 页面头部：`app/src/main/res/layout/view_page_header.xml`
- 分组壳：`app/src/main/res/layout/view_group_section.xml`
- 通用卡片 section：`app/src/main/res/layout/view_card_section.xml`
- 说明型卡片：`app/src/main/res/layout/view_section_card.xml`
- 状态条：`app/src/main/res/layout/view_status_strip.xml`
- 空态：`app/src/main/res/layout/view_empty_state.xml`
- 对话框表单容器：`app/src/main/res/layout/view_dialog_form_container.xml`
- 子女模式上传表单：`app/src/main/res/layout/view_child_mode_upload_form.xml`
- 相册上传表单：`app/src/main/res/layout/view_album_upload_form.xml`
- 相册说明 action：`app/src/main/res/layout/view_album_memory_action.xml`
- 主操作按钮：`app/src/main/res/layout/view_primary_action_button.xml`
- 设置行：`app/src/main/res/layout/view_settings_row.xml`
- 设置箭头：`app/src/main/res/layout/view_settings_row_arrow.xml`
- 设置状态：`app/src/main/res/layout/view_settings_row_status.xml`
- 详情信息行：`app/src/main/res/layout/view_detail_info_row.xml`
- 详情措施行：`app/src/main/res/layout/view_detail_bullet_row.xml`

## 4. 主线页面定位

### 4.1 登录页
入口：
- `app/src/main/java/com/silverguardian/prototype/LoginActivity.java`
- `app/src/main/res/layout/activity_login.xml`
- `app/src/main/res/layout/item_user_avatar.xml`

适合微调的点：
- 标题、副标题、PIN 卡片与登录按钮的上下间距
- 用户头像卡片尺寸与名字字号
- 添加老人按钮的高度、圆角、描边强度

### 4.2 MainActivity 底部导航壳
入口：
- `app/src/main/java/com/silverguardian/prototype/MainActivity.java`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/menu/bottom_nav_menu.xml`

适合微调的点：
- 导航整体高度：`bottom_nav_height`
- 图标大小：`bottom_nav_icon_size`
- 文案大小：`bottom_nav_text_size`
- 容器外边距：`bottom_nav_margin_horizontal`、`bottom_nav_margin_bottom`

### 4.3 首页 Home
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/HomeFragment.java`
- `app/src/main/res/layout/fragment_home.xml`

适合微调的点：
- hero 区高度和内部标题间距
- 首页功能入口行的图标盒尺寸与行高
- 首页 section 之间的上下节奏

### 4.4 健康档案页 Health
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/HealthFragment.java`
- `app/src/main/res/layout/fragment_health.xml`
- `app/src/main/res/layout/item_health_card.xml`

适合微调的点：
- 头部标题区和状态条的上下留白
- 卡片内主数据字号与侧边强调条视觉重量
- 顶部筛选、蓝牙、添加按钮的宽高与文字层级

### 4.5 用药提醒页 Medicine
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/MedicineFragment.java`
- `app/src/main/res/layout/fragment_medicine.xml`
- `app/src/main/res/layout/item_medicine.xml`

适合微调的点：
- 顶部主按钮高度、图标间距
- 状态条与筛选行的间距
- 药品卡片的内边距、标签 chip 高度与列表节奏

### 4.6 家人相册页 Album
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/AlbumFragment.java`
- `app/src/main/res/layout/item_album.xml`
- `app/src/main/res/layout/item_photo_grid.xml`
- `app/src/main/res/layout/view_album_all_photos_entry.xml`
- `app/src/main/res/layout/view_album_section_header.xml`
- `app/src/main/res/layout/view_album_empty_state.xml`
- `app/src/main/res/layout/view_album_upload_form.xml`
- `app/src/main/res/layout/view_album_memory_action.xml`
- `app/src/main/res/layout/view_primary_action_button.xml`

相册页当前结构说明：
- 页面级壳仍由 `AlbumFragment` 控制，但稳定块已优先改为 inflate 复用布局
- 搜索、创建、上传弹窗已不再手工 `new LinearLayout()` 建树
- 回忆说明 action 与主按钮已经收进复用块

适合微调的点：
- 顶部页面头部：`view_page_header.xml`
- Hero 高度：`hero_banner_height_large`
- “回忆说明”卡片位置与密度：`view_section_card.xml` + `album_memory_card_spacing_bottom`
- 底部 action row 里“上传照片”按钮：`view_primary_action_button.xml` + `action_primary_min_width` + `action_row_gap`
- 上传弹窗预览区：`view_album_upload_form.xml` + `album_upload_preview_size`
- 相册网格密度：`item_photo_grid.xml`

如果你要微调“上传照片”图标位置，优先看：
- `app/src/main/res/layout/view_primary_action_button.xml`
- `Widget.SilverGuardian.PrimaryTextAction`
- `action_row_gap`
- `button_padding_horizontal`

如果你要微调“回忆说明”位置，优先看：
- `AlbumFragment.memoryNote()` 的插入顺序
- `hero_banner_spacing_bottom`
- `album_memory_card_spacing_bottom`
- `view_section_card.xml`

### 4.7 设置页 Settings
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/SettingsFragment.java`
- `app/src/main/res/layout/fragment_settings.xml`
- `app/src/main/res/layout/view_group_section.xml`
- `app/src/main/res/layout/view_settings_row.xml`
- `app/src/main/res/layout/view_settings_row_arrow.xml`
- `app/src/main/res/layout/view_settings_row_status.xml`

适合微调的点：
- 分组标题和分组卡片之间的节奏：`group_label_spacing_*`
- 设置行高度与左右密度：`settings_row_*`
- 右侧箭头、右侧状态标签、family preview 的 end-cap 对齐感
- 个人卡片与下方各组之间的距离

### 4.8 记忆回忆页 Memory
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/MemoryFragment.java`

适合微调的点：
- section 标题和筛选行节奏
- 记忆卡片的 padding、标题字号、元信息间距
- 弹窗表单字段与列表之间的统一感

### 4.9 防诈提醒页 Fraud
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/FraudFragment.java`
- `app/src/main/res/layout/fragment_fraud.xml`
- `app/src/main/res/layout/view_status_strip.xml`

适合微调的点：
- 状态条高度与列表之间的间距
- 刷新 chip 与其他页 chip 风格的一致性
- 列表卡片层级和空态密度

### 4.10 社区便民页 CommunityFragment
入口：
- `app/src/main/java/com/silverguardian/prototype/fragments/CommunityFragment.java`
- `app/src/main/res/layout/fragment_community.xml`
- `app/src/main/res/layout/view_community_search_section.xml`

适合微调的点：
- 分类 chip 的间距与高度
- 状态提示条和搜索区域之间的节奏
- 结果列表和标题之间的呼吸感

## 5. 高价值详情页定位

### 5.1 CommunityActivity
入口：
- `app/src/main/java/com/silverguardian/prototype/CommunityActivity.java`
- `app/src/main/res/layout/activity_community.xml`
- `app/src/main/res/layout/view_community_search_section.xml`

适合微调的点：
- 页面头部与搜索区间距
- 地图区与底部返回按钮的节奏
- 搜索区中的 chips、状态条和主要操作按钮的一致性

### 5.2 PhotoDetailActivity
入口：
- `app/src/main/java/com/silverguardian/prototype/PhotoDetailActivity.java`
- `app/src/main/res/layout/activity_photo_detail.xml`
- `app/src/main/res/layout/view_detail_info_row.xml`

适合微调的点：
- 大图高度：`detail_media_height`
- 页码、副信息和标题之间的层级感
- 收藏标签与详情卡片之间的呼吸感
- 返回按钮与主线 secondary action 的一致性

### 5.3 FraudDetailActivity
入口：
- `app/src/main/java/com/silverguardian/prototype/FraudDetailActivity.java`
- `app/src/main/res/layout/activity_fraud_detail.xml`
- `app/src/main/res/layout/view_detail_bullet_row.xml`
- `app/src/main/res/layout/view_card_section.xml`

适合微调的点：
- 分类 tag、标题、正文卡片之间的上下节奏
- 正文与措施列表的 `lineSpacingExtra`
- section 标题样式是否和其他详情页一致

### 5.4 ChildModeActivity
入口：
- `app/src/main/java/com/silverguardian/prototype/ChildModeActivity.java`
- `app/src/main/res/layout/activity_child_mode.xml`
- `app/src/main/res/layout/view_child_mode_upload_form.xml`

适合微调的点：
- 分组之间的纵向间距
- 状态条和卡片间距
- 上传照片块与相册页上传表单的一致性

### 5.5 BluetoothActivity
入口：
- `app/src/main/java/com/silverguardian/prototype/BluetoothActivity.java`
- `app/src/main/res/layout/activity_bluetooth.xml`
- `app/src/main/res/layout/view_status_strip.xml`

适合微调的点：
- 状态条视觉重量
- 设备列表节奏
- 返回设置按钮与 secondary action 样式一致性

## 6. 常见视觉问题怎么找

### 6.1 按钮文字太大或太小
优先看：
- `themes.xml` 里的 `PrimaryTextAction` / `SecondaryTextAction`
- `dimens.xml` 里的 `text_body`、`secondary_action_text_size`
- 个别布局里是否单独写了 `textSize`

### 6.2 图标位置偏左、偏右、离文字太远
优先看：
- 布局是否使用 `compoundDrawable`
- `action_row_gap`
- `button_padding_horizontal`
- `gravity`
- 该按钮是走共享按钮样式，还是页面内自己定义的结构

### 6.3 section 标题和卡片距离不一致
优先看：
- `section_label_spacing_top`
- `section_label_spacing_bottom`
- `group_label_spacing_top`
- `group_label_spacing_bottom`
- `view_group_section.xml`
- `view_card_section.xml`

### 6.4 状态条有的厚、有的薄
优先看：
- `view_status_strip.xml`
- `status_strip_min_height`
- `status_strip_padding_horizontal`
- `status_strip_padding_vertical`
- `status_strip_line_spacing`

### 6.5 详情页正文可读性不一致
优先看：
- `view_detail_info_row.xml`
- `view_detail_bullet_row.xml`
- `detail_body_line_spacing`
- `TextAppearance.SilverGuardian.Body`
- `TextAppearance.SilverGuardian.DetailValue`

## 7. 推荐微调顺序

如果你要继续做视觉 polish，建议按下面顺序来，成本最低：
1. 先改 `dimens.xml` 中的高频 token
2. 再改 `themes.xml` 中的文字层级和按钮样式
3. 再看共享块 `view_*` 是否已经覆盖你要调的结构
4. 最后才去改单独页面 XML 或 Java 里的绑定逻辑

## 8. 当前结论

当前代码里，主线 UI 已经不建议再回到“Java 手工拼稳定页面结构”的方式做微调。后续视觉审计和排版微调，优先顺序应当是：
- 先 token
- 再共享块
- 再页面 XML
- 最后才碰 Java 绑定层