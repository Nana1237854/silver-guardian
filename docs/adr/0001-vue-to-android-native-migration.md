# Vue Web 应用移植为 Android 原生应用

## Status

Accepted

## Context

银发守护者当前是 Vue 3 + Vite Web 应用（PC 端），依赖 Express 后端 + MySQL + 智谱 GLM-4 API。
课程要求必须使用 Android Studio 原生开发（XML 布局、Activity/Fragment、SQLite），不允许 WebView 混合方案。

## Decision

完全独立 Android 原生 App，Express 后端和 MySQL 全部废弃：

- **UI**: XML + ViewBinding（课程第 2-3 章要求）
- **导航**: Activity + Fragment + Intent（课程第 4 章要求）
- **存储**: SQLiteOpenHelper + 手写 SQL（课程第 5 章要求）
- **网络**: HttpURLConnection + JSONObject（课程第 9 章要求）
- **异步**: Handler + Thread（课程第 9.4 节要求）
- **后台**: Service + BroadcastReceiver（课程第 7-8 章要求）
- **图片**: Glide（第三方，课程未覆盖但必需）

## Considered Options

1. **Capacitor/WebView 打包现有的 Vue 应用** — 最快但不符合课程要求，放弃。
2. **保留 Express 后端作为中转** — 减少 App 端 API 调用复杂度，但课程没有服务端要求，且与"完全独立 App"目标冲突，放弃。
3. **Jetpack Compose 声明式 UI** — 和 Vue 3 思维最接近，但课程教的是 XML 布局和 View 控件，放弃。
4. **Room ORM** — Google 推荐但课程教的是 SQLiteOpenHelper，放弃。

## Consequences

- Express 后端（mobile-vue-server）和 MySQL（smart_app）在移植后不再需要
- 10 张 MySQL 表迁为 SQLite 表 + 1 张新增药品库表（首次启动预填充）
- 智谱 API 从 App 直连，API Key 存 strings.xml
- 新增后台组件：MedicineReminderService、ReminderBroadcastReceiver、BootReceiver
- 蓝牙功能（原 Web Bluetooth API）需用 Android 原生 BluetoothGatt 重写，作为课程创新点
