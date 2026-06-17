# 课程新需求技术选型决策

## Status

Accepted

## Context

课程文档《移动软件开发考查内容及评分标准》明确要求选题二"老年人智慧生活助手 APP"实现 5 项功能和 5 项技术考察点。原原型设计仅覆盖部分，需补充新需求和对应技术点。

## Decisions

### 1. 地图 SDK：高德地图

- **选高德而非百度**：用户倾向
- **完整集成**：MapView 嵌入 + POI 关键字搜索（菜市场、社区医院、药店）+ 路径规划导航（步行/驾车）
- **权限**：`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` 运行时权限

### 2. 网络请求库：OkHttp

- **选 OkHttp 而非 Retrofit**：防诈骗 API 仅需 GET 请求，简单场景不需要 Retrofit 的注解层
- **用途**：定时拉取防诈骗知识推送内容

### 3. 适老化界面：SharedPreferences 开关 + 代码动态缩放

- **选代码方案而非双 theme**：原型大部分 UI 是代码创建，代码改尺寸比维护两套资源更轻量
- `SharedPreferences` 存 `font_mode`（normal/large/xlarge）
- `dimens.xml` 已有基准，切换后动态调整 `TextView.setTextSize()` 和 `Button.setLayoutParams()`

### 4. 一键呼叫：ACTION_CALL + 运行时权限

- **选 ACTION_CALL 而非 ACTION_DIAL**：满足"权限管理"考察点
- `CALL_PHONE` 权限在 AndroidManifest 声明 + 运行时 `requestPermissions()`
- 家属号码存 SQLite `family_members` 表，120 直接硬编码

### 5. 语音播报：系统 TTS

- **选系统内置 TTS 而非第三方**：零依赖，课程不要求第三方语音库
- `android.speech.tts.TextToSpeech` → `tts.speak("该吃降压药了", ...)`

### 6. 防诈骗推送：远程 API + OkHttp

- 数据来源：聚合数据 / 天行数据 防诈骗知识 API
- OkHttp GET 请求 → JSONObject 解析 → SQLite 缓存 + RecyclerView 展示
- 本地预置 fallback 数据（`assets/fraud_tips.json`），API 不可用时用本地

## Consequences

- 新增依赖：高德地图 SDK（3D 地图 + 搜索 + 导航约 3MB）、OkHttp（约 500KB）
- 新增权限：`CALL_PHONE`、`ACCESS_FINE_LOCATION`、`POST_NOTIFICATIONS`（Android 13+）
- 新增 Activity：CommunityMapActivity
- 新增 Fragment：FraudDetailFragment（从 FraudFragment 拆分）
- 评分覆盖：SharedPreferences ✓、SQLite ✓、RecyclerView ✓、权限管理 ✓、地图 SDK ✓、第三方网络库 ✓
