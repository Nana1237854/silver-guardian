# 银发守护者 APP 最终检查与稳定性修复更新文档

- 更新日期：2026-06-21
- 项目：Silver Guardian（银发守护者）
- 适用范围：
  - 每日平安确认
  - 异常未响应提醒
  - 家属端照护摘要
  - 一键求助增强
  - 服药后身体反馈
  - 适老化操作引导

## 1. 本次更新目标

本次更新不新增业务功能，只做项目收尾检查与稳定性修复，重点排查以下问题：

- AndroidManifest.xml 是否缺少 Activity 注册
- strings.xml 是否存在缺失资源、命名不一致或追加格式错误
- 数据库升级是否会导致旧数据丢失
- Java / XML 是否存在会导致编译失败或运行崩溃的问题
- 是否存在中文乱码污染
- 已完成的 6 个阶段功能是否还能正常联动

## 2. 本次修复结论

本轮最终检查发现并修复了 4 类关键问题：

1. `strings.xml` 缺失一批实际已被 Java 引用的字符串资源
2. `AndroidManifest.xml` 缺少 `PhotoDetailActivity` 注册，家人相册详情页存在崩溃风险
3. `ElderlyDbHelper.java` 旧版升级逻辑存在 `DROP TABLE` 风险，可能导致旧数据丢失
4. `SettingsFragment.java` 中存在乱码注释，属于编码污染

## 3. 修改文件

本次最终检查直接修改了以下文件：

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/silverguardian/prototype/data/ElderlyDbHelper.java`
- `app/src/main/java/com/silverguardian/prototype/fragments/SettingsFragment.java`
- `app/src/main/res/values/strings.xml`

## 4. 具体修复内容

### 4.1 AndroidManifest.xml

修复内容：

- 补充注册：
  - `PhotoDetailActivity`
- 检查并确认以下 Activity 已注册：
  - `LoginActivity`
  - `MainActivity`
  - `ChatDetailActivity`
  - `ChildModeActivity`
  - `BluetoothActivity`
  - `CommunityActivity`
  - `MedicineLibraryActivity`
  - `FraudDetailActivity`
  - `FamilyManageActivity`
  - `CareSummaryActivity`
  - `PhotoDetailActivity`

导出策略检查结果：

- `LoginActivity` 作为启动页，`android:exported="true"` 合理
- 其他项目内部页面均为 `android:exported="false"`，设置合理

### 4.2 strings.xml

修复内容：

- 保持原文件结构不重排、不覆盖，只在 `</resources>` 前追加缺失资源
- 补齐此前已被 Java 引用、但实际不存在的字符串
- 修复末尾追加字符串时的换行格式，确保 XML 可被正常解析

本次补齐的字符串主要包括：

- 家属联系人管理：
  - `settings_manage_family_title`
  - `settings_manage_family_desc`
  - `family_manage_title`
  - `family_manage_add_button`
  - `family_manage_add_title`
  - `family_manage_edit_title`
  - `family_manage_delete_title`
  - `family_manage_delete_message`
  - `family_manage_name_label`
  - `family_manage_name_hint`
  - `family_manage_name_required`
  - `family_manage_relationship_label`
  - `family_manage_relationship_hint`
  - `family_manage_relationship_required`
  - `family_manage_phone_label`
  - `family_manage_phone_hint`
  - `family_manage_phone_required`
  - `family_manage_phone_too_short`
- 防诈提醒与防诈详情：
  - `fraud_daily_reminder_title`
  - `fraud_daily_reminder_desc`
  - `fraud_notification_permission_message`
  - `fraud_notification_title`
  - `fraud_offline_badge`
  - `fraud_offline_hint_detail`
  - `fraud_detail_summary_title`
  - `fraud_detail_risk_title`
  - `fraud_detail_advice_title`
  - `fraud_detail_source_title`
  - `fraud_source_label_name`
  - `fraud_source_label_date`
  - `fraud_source_label_type`
  - `fraud_source_local`
  - `fraud_source_official`

检查结果：

- `strings.xml` 已验证为合法 XML
- Java 层 `R.string.*` 引用检查结果为 `NO_MISSING_STRINGS`

### 4.3 ElderlyDbHelper.java

修复目标：

- 保证数据库升级不丢失旧数据
- 保证新增表在新安装和旧版本升级时都能正常创建
- 不修改任何中文 seed 数据

修复内容：

- 保持 `DB_VERSION = 5`，不再继续递增
- `createBusinessTables()` 中相关业务表改为 `CREATE TABLE IF NOT EXISTS`
- 相关索引改为 `CREATE INDEX IF NOT EXISTS`
- `oldVersion < 2` 的升级逻辑取消 `DROP TABLE`
- 改为：
  - 先判断 `users` 是否缺少 `hint_question`
  - 再判断是否缺少 `hint_answer`
  - 缺少时才执行 `ALTER TABLE`
  - 然后调用 `createBusinessTables(db)` 做安全补齐

本次确认保留的新增表：

- `safe_check_records`
- `medicine_feedback`

本次确认未破坏的中文种子数据：

- `medicine_library`
- `fraud_tips`

### 4.4 SettingsFragment.java

修复内容：

- 清理了乱码注释
- 保留并确认以下功能可用：
  - 防诈提醒开关
  - Android 13 通知权限兜底
  - “重新查看操作引导”入口

## 5. 数据库版本与表结构说明

### 5.1 当前数据库版本

- 当前版本：`DB_VERSION = 5`

### 5.2 与照护闭环扩展相关的新增表

#### `safe_check_records`

用途：

- 保存每日平安确认状态
- 支持按用户、按日期隔离记录
- 为异常未响应提醒和照护摘要提供数据来源

关键字段：

- `id`
- `user_id`
- `status`
- `note`
- `checked_at`
- `record_date`

#### `medicine_feedback`

用途：

- 保存服药后的身体反馈
- 支持统计今日不适反馈数量
- 为照护摘要展示服药后状态提供依据

关键字段：

- `id`
- `user_id`
- `medicine_id`
- `medicine_name`
- `feedback_type`
- `feedback_text`
- `date`
- `created_at`

## 6. 模块与类清单

### 6.1 新增 Model

- `SafeCheckRecord`
- `CareSummary`
- `SosHelpInfo`
- `MedicineFeedback`

### 6.2 新增 Dao

- `SafeCheckDao`
- `MedicineFeedbackDao`

### 6.3 新增 Module

- `SafeCheckModule`
- `CareSummaryModule`
- `SosHelpModule`
- `MedicineFeedbackModule`
- `SeniorGuideModule`

### 6.4 新增 / 重点页面

- `CareSummaryActivity`
- `FamilyManageActivity`
- `FraudDetailActivity`
- `PhotoDetailActivity`

## 7. 功能回归检查结果

本轮按源码、资源、Manifest、数据库关系进行回归核对，确认以下功能链路仍然成立：

1. 首页
2. 健康档案
3. 用药提醒
4. 家人相册
5. 家属管理
6. 防诈提醒
7. 一键呼叫 / SOS
8. 设置页
9. 今日照护摘要
10. 每日平安确认
11. 服药后身体反馈
12. 适老化操作引导

说明：

- 本轮没有删除原有功能
- 没有引入 Room
- 没有重写 Repository
- 没有修改无关页面业务逻辑

## 8. 编译检查结果

### 8.1 已完成的检查

- `strings.xml` 合法性检查通过
- `AndroidManifest.xml` 合法性检查通过
- `R.string.*` 资源引用完整性检查通过
- `@drawable/...` 缺失检查通过
- 中文乱码特征扫描通过
- `ElderlyDbHelper.java` 保持合法 Java 结构

### 8.2 当前未完成的完整编译原因

当前环境下，Gradle 编译仍被本机 `app/build` 目录文件锁阻塞，报错属于系统文件占用，而不是新引入的源码错误。

已观察到的锁文件报错包括：

- `mapDebugSourceSetPaths` 阶段的 `file-map.txt`
- `mergeDebugResources` 阶段的 `color_nav_item_tint.xml.flat`
- `compileDebugJavaWithJavac` 阶段的 `HealthRecordModule.class` stash 事务

结论：

- 目前源码层面主要问题已修复
- 剩余阻塞来自本机构建缓存占用，不属于本次功能修改逻辑本身

## 9. Android Studio 验证步骤

建议按以下顺序验证：

1. 停止正在运行的 App、Gradle 任务和相关预览
2. 执行 `Build > Clean Project`
3. 如仍有 `AccessDeniedException`，关闭 Android Studio 后删除 `app/build`
4. 重新打开项目，执行 `Build > Rebuild Project`
5. 手动回归以下流程：
   - 登录进入首页
   - 首页平安确认卡片
   - 首页 SOS / 一键求助弹窗
   - 用药提醒页完成打卡并填写服药反馈
   - 设置页进入“今日照护摘要”
   - 家属联系人管理
   - 家人相册点击照片进入详情页
   - 防诈提醒与防诈详情页
   - 设置页点击“重新查看操作引导”

## 10. 答辩展示建议顺序

建议答辩时按“照护闭环”顺序展示：

1. 首页总览
2. 每日平安确认
3. 异常未响应提醒
4. 用药提醒
5. 服药后身体反馈
6. 今日照护摘要
7. 一键求助增强
8. 家属联系人管理
9. 家人相册与详情页
10. 防诈提醒与案例详情
11. 设置页中的操作引导重置
12. 最后补充数据库升级与本地通知设计

## 11. 总结

本次更新的核心价值不是继续扩功能，而是把已有 6 个阶段的成果“收口”：

- 补齐缺失资源
- 修复 Manifest 注册遗漏
- 消除数据库升级丢数据风险
- 清理乱码污染
- 保障前后端接入后的整体稳定性

截至本次更新，项目的照护闭环扩展功能在结构上已经完整，后续主要工作将集中在：

- Android Studio 本机构建锁问题清理
- 真机 / 模拟器联调验证
- 最终答辩演示流程打磨
