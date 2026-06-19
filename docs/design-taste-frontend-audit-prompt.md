# 银发守护者 — design-taste-frontend 审计提示词

## 使用方法

在 Codex 中分步使用。每步等待确认后再继续下一步。

---

## 提示词

```
/design-taste-frontend

Brief:
- Project: 银发守护者 (Silver Guardian) — Android native app for elderly health companion
- Source: D:\Android Studio\app\src\main\res\layout\ + app/src/main/java/com/silverguardian/prototype/
- Mode: preserve brand
- Audience: 60–80 year old Chinese seniors (primary), their adult children (secondary, via Child Mode)
- What works today:
  1. Jade-green color system (#2FA579 primary, #F4FAF7 page, #176B50 dark) — calm, trustworthy, non-clinical
  2. 5-level type scale (32/26/21/17/14sp) with bold weight contrast — readable at arm's length
  3. 22dp card radius + 28dp button radius + 48dp minimum touch targets — deliberate shape language
- What is broken today:
  1. Several screens build UI programmatically (Java code) instead of XML — inconsistent tooling
  2. "全部照片" album entry has no visual weight distinction from category albums
  3. AI chat system prompt and health keywords are hardcoded in Java, not configurable
- Navigation constraint: keep all 5 bottom-nav tabs (AI/Health/Medicine/Album/Settings), their labels, and the LoginActivity→MainActivity→ChildModeActivity flow unchanged
```

### Design Tokens（来自 UI_DESIGN_SYSTEM.md）

```
Primary: #2FA579 | Primary Dark: #176B50 | Primary Light: #DDF3E9
Page bg: #F4FAF7 | Surface: #FFFFFF | Text Primary: #18221D | Text Secondary: #58665F
Divider: #DDE7E1 | Coral accent: #F48A64 (warnings only) | SOS red: #B42318
Font: Android system sans-serif Chinese (no downloaded fonts)
Type scale: 32sp display / 26sp title / 21sp subtitle / 17sp body / 14sp small
Spacing: 4dp base grid → 8/12/16/18/24dp
Radius: card 22dp / button 28dp / bottom-nav 34dp
Touch target: ≥48dp | Button height: 56dp
Shadow: 1–2dp light shadows only on floating nav and key cards
```

### Domain Glossary（来自 CONTEXT.md）

| 术语 | 英文 | 说明 |
|------|------|------|
| 老人 | Elder | 主要使用者，通过 PIN 码登录 |
| 家属 | Family Member | 与老人关联的子女，在子女模式中查看数据 |
| 健康档案 | Health Record | 健康指标记录（血压/血糖/心率/步数等） |
| 用药提醒 | Medicine Reminder | 定时用药计划 + 打卡记录 |
| 亲情相册 | Family Album | 家属上传照片 + 老人浏览 |
| 智能对话 | AI Chat | 智谱 GLM-4 驱动的健康咨询 |
| 一键呼叫 | One-Tap Call | 直接拨号到家属或 120 |
| 防诈骗推送 | Fraud Prevention | 远程拉取反诈骗知识并每日推送 |
| SOS 紧急呼叫 | SOS Emergency | 弹窗确认后拨打或发短信 |
| 服药打卡 | Medicine Check-in | 老人确认已服药的每日记录 |
| 适老化主题 | Elder-Friendly Theme | 大字模式 + 高对比度配色 |
| 社区便民查询 | Community POI Search | 高德 SDK 搜索附近菜市场/药店/医院 |

---

## Step 1 — 审计

```
Step 1. Run the Section 11 audit:
- Brand tokens currently in use (primary, accent, type stack, radii)
- Information architecture (activity tree, navigation, user flows)
- Patterns to preserve (large-text mode toggle, bottom nav 5-tab, PIN login grid)
- Patterns to retire (programmatic UI builders, hardcoded strings in Java, missing empty states)
- Inferred dial reading (DESIGN_VARIANCE, MOTION_INTENSITY, VISUAL_DENSITY)
- Screen inventory: list every screen with its design quality score (A–F)
Post the audit in writing. Stop.
```

**确认后继续 Step 2。**

---

## Step 2 — 模式选择

```
Step 2 (after my OK). Declare the mode (Preserve, Overhaul, or Greenfield-with-content-preserved) and which modernisation levers you will apply, in priority order. Stop.
```

**确认后继续 Step 3。**

---

## Step 3 — 实施

```
Step 3 (after my OK). Implement the changes. Keep bottom-nav tab labels, Activity class names, intent filters, form field names, brand logo asset names, and CONTEXT.md terminology unchanged unless I explicitly approve a change.
```

**确认后继续 Step 4。**

---

## Step 4 — 验证

```
Step 4. Run in writing:
- Hardcoded-value audit: list every raw dp/sp value in Java files that should reference dimens.xml
- Pre-Flight Check (Section 14)
- Preservation audit: list every Activity name, nav label, intent filter, and string resource changed. Should be empty unless I approved.
- Brand fidelity audit: confirm the jade-green primary, 5-level type scale, 22/28/34dp radii, and 48dp touch targets survived.
Any Fail blocks completion.
```

---

## 安全护栏

以下内容在任何情况下**不可修改**（除非明确批准）：

| 层级 | 不可改内容 |
|------|-----------|
| 导航 | 5 个底部 Tab 标签、LoginActivity→MainActivity→ChildModeActivity 流程 |
| 类名 | 所有 Activity/Fragment/Adapter 类名 |
| Intent | Intent filter 配置、extra key 名称 |
| 表单 | EditText field name、表单验证逻辑 |
| 品牌 | logo 资源文件名、主色 #2FA579 |
| 术语 | CONTEXT.md 定义的全部领域术语 |
| 字符串 | `strings.xml` 中已有的 string name 不可删除或重命名 |
