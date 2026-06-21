# Bug 修复：家属管理入口 + 相册创建

> 日期：2026-06-21  
> 项目：银发守护者（Silver Guardian）

---

## Context

**Bug 1**：FamilyMember 整个写入链路缺失——FamilyDao 只有 readAll/seed（硬编码），Repository/UserSessionModule/UI 全是只读。老人和子女找不到任何入口来添加真实的家属姓名和联系电话。

**Bug 2**：点"创建新相册"只设置了一个内存字符串 currentAlbum，没有任何持久化。相册靠 photo.category 文本派生，空相册不可能存在。附带子 bug：AlbumFragment 上传照片 path 为空。

---

## 决策汇总

| # | 决策 | 选择 |
|---|------|:---:|
| 1 | 家属录入入口 | SettingsFragment"家人与设备"→ 新 Activity |
| 2 | UI 形态 | 新 FamilyManageActivity |
| 3 | 家属 CRUD 程度 | 完整增删改：添加弹窗 + 点击编辑 + 长按删除 |
| 4 | 相册修复方案 | 新建 albums 表，独立实体 |
| 5 | photo-album 关联 | album_photos 去 category，改 album_id 外键 |
| 6 | 空相册展示 | 占位封面 + "0 张" + 上传引导 |

---

## 修改文件清单

### Bug 1 — 家属管理（11 个文件）

| # | 文件 | 操作 | 说明 |
|---|------|:---:|------|
| 1 | `data/dao/FamilyDao.java` | 修改 | 新增 add() / update() / delete() 方法 |
| 2 | `data/Repository.java` | 修改 | 新增 addFamilyMember() / updateFamilyMember() / deleteFamilyMember()，含内存列表同步 |
| 3 | `modules/UserSessionModule.java` | 修改 | 新增家属写方法代理 |
| 4 | `FamilyManageActivity.java` | **新建** | 家属列表管理页（RecyclerView + 添加/编辑/删除） |
| 5 | `res/layout/activity_family_manage.xml` | **新建** | 页面布局：header + 空状态 + RecyclerView |
| 6 | `res/layout/item_family_member.xml` | **新建** | 家属列表项：姓名 / 关系 / 电话 / 编辑箭头 |
| 7 | `res/values/strings.xml` | 修改 | 新增家属管理相关字符串 |
| 8 | `fragments/SettingsFragment.java` | 修改 | "家人与设备"区新增"管理家属联系人"行 |
| 9 | `AndroidManifest.xml` | 修改 | 注册 FamilyManageActivity |
| 10 | `proguard-rules.pro` | 修改 | keep FamilyManageActivity |
| 11 | `DESIGN.md` | — | UI 设计遵循现有规范，不修改 |

### Bug 2 — 相册创建（10 个文件）

| # | 文件 | 操作 | 说明 |
|---|------|:---:|------|
| 12 | `data/ElderlyDbHelper.java` | 修改 | 新建 albums 表；album_photos 改 album_id 外键；DB version 3 迁移 |
| 13 | `models/Album.java` | **新建** | 相册实体：id / name / coverUrl / photoCount / createdAt |
| 14 | `data/dao/AlbumDao.java` | **新建** | albums 表 CRUD |
| 15 | `models/AlbumPhoto.java` | 修改 | category → albumId |
| 16 | `data/dao/AlbumDao.java`（现有） | 修改 | album_photos 读写适配 albumId |
| 17 | `data/Repository.java` | 修改 | 新增 addAlbum() / deleteAlbum()；albumPhoto 读写适配 albumId |
| 18 | `modules/FamilyAlbumModule.java` | 修改 | 新增 createAlbum()；上传适配 albumId |
| 19 | `fragments/AlbumFragment.java` | 修改 | 创建相册 → 调 createAlbum()；列表展示 Album 实体；空相册占位；修复上传 path bug |
| 20 | `res/layout/item_album.xml` | 修改 | 空相册占位封面 + 0 张照片 |
| 21 | `res/values/strings.xml` | 修改 | 新增相册相关字符串 |

---

## 详细方案

### Bug 1 实现细节

**FamilyDao 新增方法：**

```java
public long add(SQLiteDatabase db, int userId, String name, String relationship, String phone)
public int update(SQLiteDatabase db, int id, String name, String relationship, String phone)
public void delete(SQLiteDatabase db, int id)
```

**Repository 新增方法：**

- `addFamilyMember(name, relationship, phone)` — 写 DB + 内存列表同步 + 返回新 FamilyMember
- `updateFamilyMember(id, name, relationship, phone)` — 写 DB + 内存列表同步
- `deleteFamilyMember(id)` — 写 DB + 内存列表移除

**UserSessionModule 新增方法：**

- 代理 Repository 的三个写方法

**FamilyManageActivity 设计（遵循 DESIGN.md）：**

- 顶部：view_page_header（标题"家属联系人"，左侧返回按钮）
- 空状态：view_empty_state（"还没有添加家属联系人"）
- 右上角 header_action：`+` 按钮（ic_add），点击弹添加弹窗
- 列表：RecyclerView，每项显示姓名/关系/电话
- 弹窗：AlertDialog，姓名 + 关系 + 电话三个输入框
- 点击项：弹编辑弹窗（预填当前值）
- 长按项：确认删除弹窗
- 样式：body 17sp，jade 主色，12dp 圆角，白色表面 + 1dp divider

**SettingsFragment 改动：**

- 在"家人与设备"组现有的"子女模式"行下方，新增一行"管理家属联系人"（ic_family 图标），点击跳转 FamilyManageActivity

---

### Bug 2 实现细节

**albums 表（ElderlyDbHelper DB_VERSION = 3）：**

```sql
CREATE TABLE albums (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  cover_url TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

**album_photos 改动：**

- 删除 `category` 列
- 新增 `album_id INTEGER NOT NULL` 外键 → albums(id)
- 迁移逻辑：根据旧 category 值，在 albums 表中查找或创建对应记录，映射 album_id

**AlbumDao（新建）：**

- `create(userId, name)` → 返回 albumId
- `readAll(userId)` → 返回 List<Album>（含 photoCount 子查询）
- `delete(albumId)` → 级联删照片
- `updateCover(albumId, coverUrl)` → 更新封面

**Album 模型（新建）：**

```java
public class Album {
    public int id;
    public String name;
    public String coverUrl;    // 最新一张照片 URL，空则用占位
    public int photoCount;
    public String createdAt;
}
```

**AlbumFragment 改动：**

- 列表数据源从 `List<AlbumPhoto>` 改为 `List<Album>`
- "创建新相册" → `familyAlbum().createAlbum(name)` → 刷新列表 → 新卡片立即可见
- 空相册卡片：ic_album 占位图 + 相册名 + "0 张照片"
- 点击空相册 → 显示上传引导 + "上传第一张照片"按钮
- 修复上传 path bug：改用 `PhotoStorage.save()` + 6 参 addPhoto()

**FamilyAlbumModule 新增：**

- `createAlbum(name)` → Repository.addAlbum() → 返回 Album
- `addPhoto()` 适配 albumId 参数

**ChildModeActivity 上传适配：**

- `showUploadDialog()` 中 category 改为 albumId

---

## DB 迁移说明

`ElderlyDbHelper.onUpgrade()` DB 2 → 3：

1. 创建 albums 表
2. 读取所有现有 album_photos 行，按 category 分组
3. 为每个唯一 category 在 albums 表中创建记录
4. 创建新的 album_photos 表（category → album_id）
5. 将旧数据按 category → album_id 映射迁移到新表
6. 删除旧 album_photos 表，重命名新表

---

## 验证方法

### Bug 1 — 家属管理

1. 设置 → 家人与设备 → 点击"管理家属联系人" → 进入 FamilyManageActivity
2. 空列表显示空状态提示
3. 点 `+` → 弹窗填写姓名/关系/电话 → 确认 → 列表新增一条
4. 点击已有条目 → 弹编辑弹窗（预填值）→ 修改 → 确认 → 列表刷新
5. 长按条目 → 确认删除 → 列表移除
6. 返回设置页重新进入 → 数据持久化存在

### Bug 2 — 相册创建

1. 家人相册 → 点"创建新相册" → 输入名称 → 确认 → 列表中立即出现新相册卡片（占位封面 + 0 张）
2. 点击空相册 → 显示空状态 + "上传第一张照片"
3. 点击上传 → 选照片 → 上传成功 → 相册卡片封面更新 + 数量变为 1
4. 删除有照片的相册 → 确认 → 相册及照片一并删除
5. 旧版本升级到新版 → 原有照片按 category 自动归入对应相册
