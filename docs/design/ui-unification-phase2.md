# UI Unification Phase 2

## Summary

本轮完成了 UI 主线的最后一批收尾工作，目标保持不变：统一到“页面级 XML + 局部复用 layout + Java 只负责绑定、状态、交互”的模式。

当前主线与高价值详情页已经完成结构收口：
- Login
- MainActivity 底部导航壳
- HomeFragment
- HealthFragment
- MedicineFragment
- AlbumFragment
- MemoryFragment
- SettingsFragment
- CommunityFragment
- FraudFragment
- CommunityActivity
- PhotoDetailActivity
- FraudDetailActivity
- ChildModeActivity
- BluetoothActivity
- ChatDetailActivity

## This Round

### 1. AlbumFragment 最终收口

本轮继续把相册页剩余的稳定结构从 Java 挪到 XML：
- 搜索弹窗与创建相册弹窗统一复用 `app/src/main/res/layout/view_dialog_form_container.xml`
- 上传照片弹窗改为复用 `app/src/main/res/layout/view_album_upload_form.xml`
- 回忆说明 action 从通用 `TextView` 动态拼装收口到 `app/src/main/res/layout/view_album_memory_action.xml`
- 主操作按钮统一走 `app/src/main/res/layout/view_primary_action_button.xml`

收口后，`AlbumFragment` 里保留在 Java 的内容主要只剩：
- 相册数据筛选与排序
- 图片加载与权限回调
- 弹窗行为与保存逻辑
- RecyclerView 绑定

稳定布局结构不再由 `new LinearLayout()` 或 `new TextView()` 承担。

### 2. SettingsFragment 最终收口

设置页最后一批 row end-cap 也已经统一：
- 右侧箭头抽到 `app/src/main/res/layout/view_settings_row_arrow.xml`
- 状态型 toggle 抽到 `app/src/main/res/layout/view_settings_row_status.xml`
- 行壳继续统一使用 `app/src/main/res/layout/view_settings_row.xml`
- 分组壳继续统一使用 `app/src/main/res/layout/view_group_section.xml`

收口后，`SettingsFragment` 中 Java 只负责：
- 绑定图标、标题、副标题
- 注入右侧控制项
- 处理点击、跳转、字体大小与高对比度状态

### 3. Token 与同类块最终对齐

本轮没有引入新的设计方向，只对高频 token 和共享块做最终对齐。

新增或固化的重点：
- `section_card_padding_horizontal`
- `section_card_padding_top`
- `section_card_padding_bottom`
- `section_card_body_gap_top`
- `section_card_body_gap_bottom`
- `settings_row_*`
- `status_strip_*`
- `chip_*` / `compact_chip_*`
- `action_row_gap`
- `detail_body_line_spacing`

同步落到共享样式与局部块：
- `Widget.SilverGuardian.CardActionText`
- `Widget.SilverGuardian.SettingsRowArrow`
- `Widget.SilverGuardian.SettingsRowStatus`
- `view_status_strip.xml`
- `view_section_card.xml`
- `view_settings_row.xml`

## New Or Updated Shared Blocks

本阶段确认可复用、且已经稳定落地的局部块包括：
- `app/src/main/res/layout/view_page_header.xml`
- `app/src/main/res/layout/view_group_section.xml`
- `app/src/main/res/layout/view_card_section.xml`
- `app/src/main/res/layout/view_section_card.xml`
- `app/src/main/res/layout/view_status_strip.xml`
- `app/src/main/res/layout/view_empty_state.xml`
- `app/src/main/res/layout/view_dialog_form_container.xml`
- `app/src/main/res/layout/view_child_mode_upload_form.xml`
- `app/src/main/res/layout/view_album_upload_form.xml`
- `app/src/main/res/layout/view_album_memory_action.xml`
- `app/src/main/res/layout/view_primary_action_button.xml`
- `app/src/main/res/layout/view_settings_row.xml`
- `app/src/main/res/layout/view_settings_row_arrow.xml`
- `app/src/main/res/layout/view_settings_row_status.xml`
- `app/src/main/res/layout/view_detail_info_row.xml`
- `app/src/main/res/layout/view_detail_bullet_row.xml`

## Verification

本轮固定验收项：
- `:app:processDebugResources`
- `:app:compileDebugJavaWithJavac`
- 如环境允许，再补 `:app:assembleDebug`

代码侧补充复查项：
- `AlbumFragment.java` 与 `SettingsFragment.java` 中不再保留用于稳定页面结构的 `new TextView()` / `new LinearLayout()`
- 本轮涉及页面的 UI 文案继续以资源化为主
- 文档文件统一为 UTF-8 无 BOM

## Outcome

到本阶段结束，UI 主线已经基本完成架构意义上的统一：
- 页面级 XML 负责稳定结构
- `view_*` / `item_*` 负责局部复用
- Java 负责绑定、状态、权限、事件、导航

## Post-Phase Candidates

以下页面不作为当前批次遗留，而是明确后移候选：
- 更边缘的旧工具页
- 不在 UI 主线内的历史详情页
- 非 UI 主线范围的全仓 strings 治理
- 设计层面的新视觉方向升级