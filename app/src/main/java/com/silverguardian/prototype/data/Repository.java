package com.silverguardian.prototype.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.dao.*;
import com.silverguardian.prototype.health.HealthAlertService;
import com.silverguardian.prototype.models.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据层统一入口 (Repository 模式)。
 * 替代 MockData 的 God Object，内部委托给各领域 DAO。
 */
public class Repository {
    private static Repository instance;

    private final Context appContext;
    private final ElderlyDbHelper dbHelper;
    private final UserDao userDao;
    private final HealthDao healthDao;
    private final MedicineDao medicineDao;
    private final AlbumDao albumDao;
    private final ChatDao chatDao;
    private final MemoryDao memoryDao;
    private final EmergencyDao emergencyDao;
    private final FamilyDao familyDao;

    // 内存缓存（避免每次读取都查 DB）
    private final List<User> users = new ArrayList<>();
    private final List<HealthData> healthData = new ArrayList<>();
    private final List<FamilyMember> familyMembers = new ArrayList<>();
    private final List<Medicine> medicines = new ArrayList<>();
    private final List<AlbumPhoto> photos = new ArrayList<>();
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final List<MemoryRecord> memories = new ArrayList<>();
    private final List<EmergencyAlert> emergencyAlerts = new ArrayList<>();
    private final List<FraudTip> fraudTips = new ArrayList<>();
    private final List<MedicineLibraryItem> medicineLibrary = new ArrayList<>();
    private final List<BluetoothDeviceMock> bluetoothDevices = new ArrayList<>();

    private boolean loaded = false;
    private int activeUserId = 1;

    private Repository(Context context) {
        appContext = context.getApplicationContext();
        dbHelper = new ElderlyDbHelper(appContext);
        userDao = new UserDao(dbHelper);
        healthDao = new HealthDao(dbHelper);
        medicineDao = new MedicineDao(dbHelper);
        albumDao = new AlbumDao(dbHelper);
        chatDao = new ChatDao(dbHelper);
        memoryDao = new MemoryDao(dbHelper);
        emergencyDao = new EmergencyDao(dbHelper);
        familyDao = new FamilyDao(dbHelper);
    }

    public static synchronized Repository init(Context context) {
        if (instance == null) instance = new Repository(context);
        instance.load();
        return instance;
    }

    public static Repository getInstance() {
        if (instance == null) throw new IllegalStateException("Repository not initialized. Call init(context) first.");
        return instance;
    }

    // ========== Load ==========

    private void load() {
        seedStaticLists();
        users.clear(); healthData.clear(); familyMembers.clear(); medicines.clear();
        photos.clear(); chatMessages.clear(); memories.clear(); emergencyAlerts.clear();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        seedDatabaseIfEmpty(db);

        users.addAll(userDao.readAll());
        if (!findUserById(activeUserId) && !users.isEmpty()) activeUserId = users.get(0).id;

        healthData.addAll(healthDao.readAll(activeUserId));
        familyMembers.addAll(familyDao.readAll(activeUserId));
        medicines.addAll(medicineDao.readAll(activeUserId, today()));
        photos.addAll(albumDao.readAll(activeUserId));
        chatMessages.addAll(chatDao.readAll(activeUserId));
        memories.addAll(memoryDao.readAll(activeUserId));
        emergencyAlerts.addAll(emergencyDao.readAll(activeUserId));
        loaded = true;
    }

    private void seedDatabaseIfEmpty(SQLiteDatabase db) {
        if (!usersReadable(db)) {
            userDao.seedIfEmpty(db);
            long userId = 1;
            healthDao.seed(db, userId);
            familyDao.seed(db, userId);
            medicineDao.seed(db, userId, today());
            albumDao.seed(db, userId);
            chatDao.seed(db, userId);
            memoryDao.seed(db, userId);
            emergencyDao.seed(db, userId);
        }
    }

    private boolean usersReadable(SQLiteDatabase db) {
        android.database.Cursor c = db.rawQuery("SELECT COUNT(*) FROM users", null);
        boolean has = c.moveToFirst() && c.getInt(0) > 0;
        c.close();
        return has;
    }

    private boolean findUserById(int id) {
        for (User u : users) if (u.id == id) return true;
        return false;
    }

    // ========== Static data ==========

    private void seedStaticLists() {
        if (!medicineLibrary.isEmpty()) return;
        medicineLibrary.add(new MedicineLibraryItem(1, "高血压", "硝苯地平缓释片", "拜新同", "降压药", "用于治疗高血压和心绞痛，需遵医嘱服用。"));
        medicineLibrary.add(new MedicineLibraryItem(2, "高血压", "缬沙坦胶囊", "代文", "降压药", "用于轻、中度原发性高血压。"));
        medicineLibrary.add(new MedicineLibraryItem(3, "糖尿病", "二甲双胍片", "格华止", "降糖药", "用于 2 型糖尿病血糖控制。"));
        medicineLibrary.add(new MedicineLibraryItem(4, "冠心病", "阿司匹林肠溶片", "拜阿司匹灵", "心血管药", "抗血小板聚集，使用前需确认禁忌。"));
        medicineLibrary.add(new MedicineLibraryItem(5, "骨质疏松", "碳酸钙D3片", "钙尔奇", "维生素", "补充钙和维生素 D。"));
        medicineLibrary.add(new MedicineLibraryItem(6, "感冒", "对乙酰氨基酚片", "泰诺林", "感冒药", "用于发热、头痛等症状。"));
        medicineLibrary.add(new MedicineLibraryItem(7, "咳嗽", "氨溴索口服液", "沐舒坦", "止咳药", "帮助稀释痰液，缓解咳嗽。"));
        medicineLibrary.add(new MedicineLibraryItem(8, "胃痛", "奥美拉唑胶囊", "洛赛克", "胃药", "用于胃酸相关不适。"));
        if (fraudTips.isEmpty()) {
            fraudTips.add(new FraudTip(1, "冒充客服退款", "电信诈骗", "陌生人要求提供验证码或屏幕共享时，先挂断并联系家属。", "不要透露验证码"));
            fraudTips.add(new FraudTip(2, "保健品讲座陷阱", "保健品", "免费礼品后推销高价保健品，通常不是正规医疗建议。", "买药前问医生"));
            fraudTips.add(new FraudTip(3, "假冒亲友借钱", "亲情诈骗", "收到紧急借钱消息时，先电话确认本人身份。", "先电话确认"));
            fraudTips.add(new FraudTip(4, "中奖缴费骗局", "中奖诈骗", "中奖要求先交手续费或保证金，基本都是骗局。", "不转账"));
        }
        if (bluetoothDevices.isEmpty()) {
            bluetoothDevices.add(new BluetoothDeviceMock(1, "臂式血压计 BP-80", "blood_pressure", "126/81", "可连接"));
            bluetoothDevices.add(new BluetoothDeviceMock(2, "指夹血氧仪 OX-2", "blood_oxygen", "98", "可连接"));
            bluetoothDevices.add(new BluetoothDeviceMock(3, "智能手环 HR-6", "heart_rate", "74", "可连接"));
        }
    }

    // ========== Getters ==========

    public List<User> getUsers() { return users; }
    public List<HealthData> getHealthData() { return healthData; }
    public List<FamilyMember> getFamilyMembers() { return familyMembers; }
    public List<Medicine> getMedicines() { return medicines; }
    public List<AlbumPhoto> getPhotos() { return photos; }
    public List<ChatMessage> getWelcomeMessages() { return chatMessages; }
    public List<MemoryRecord> getMemories() { return memories; }
    public List<FraudTip> getFraudTips() { return fraudTips; }
    public List<EmergencyAlert> getEmergencyAlerts() { return emergencyAlerts; }
    public List<MedicineLibraryItem> getMedicineLibrary() { return medicineLibrary; }
    public List<BluetoothDeviceMock> getBluetoothDevices() { return bluetoothDevices; }

    // ========== Active User ==========

    public void setActiveUser(int userId) {
        activeUserId = userId;
        load();
    }

    // ========== User CRUD ==========

    public User addUser(String name, String pin, int age, String conditions) {
        int id = userDao.add(name, pin, age, conditions);
        User user = new User(id, name, pin, "", age, conditions);
        users.add(user);
        return user;
    }

    public void deleteUser(User user) {
        users.remove(user);
        userDao.delete(user.id);
    }

    // ========== Health CRUD ==========

    public HealthData addHealthData(String type, String value, String status) {
        HealthData data = new HealthData(type, value, status, R.drawable.ic_info);
        healthData.add(0, data);
        healthDao.add(activeUserId, type, value, status);
        HealthAlertService.checkAndAlert(appContext, data);
        return data;
    }

    public void deleteHealthData(HealthData data) { healthData.remove(data); }

    // ========== Medicine CRUD ==========

    public Medicine addMedicine(String name, String type, String time, String method, String description) {
        int id = medicineDao.add(activeUserId, name, type, time, method, description);
        Medicine medicine = new Medicine(id, name, type, time, method, description, "", false);
        medicines.add(0, medicine);
        return medicine;
    }

    public void deleteMedicine(Medicine medicine) {
        medicines.remove(medicine);
        medicineDao.delete(medicine.id);
    }

    public void toggleMedicineTaken(Medicine medicine, boolean taken) {
        medicine.takenToday = taken;
        medicineDao.setTaken(activeUserId, medicine.id, taken, today());
    }

    // ========== Album CRUD ==========

    public AlbumPhoto addPhoto(String title, String category, String message) {
        int id = albumDao.add(activeUserId, title, category, message);
        AlbumPhoto photo = new AlbumPhoto(id, "", title, "家属上传照片", category, "家属", false, category, message);
        photos.add(0, photo);
        return photo;
    }

    public void deletePhoto(AlbumPhoto photo) {
        photos.remove(photo);
        albumDao.delete(photo.id);
    }

    public void toggleFavorite(AlbumPhoto photo) {
        photo.favorite = !photo.favorite;
        albumDao.setFavorite(photo.id, photo.favorite);
    }

    // ========== Chat ==========

    public void addChatMessage(String text, String type) {
        chatMessages.add(new ChatMessage(text, type, now()));
        chatDao.add(activeUserId, text, type);
    }

    // ========== Memory CRUD ==========

    public MemoryRecord addMemory(String content, String category) {
        int id = memoryDao.add(activeUserId, content, category);
        MemoryRecord record = new MemoryRecord(id, content, category, "刚刚");
        memories.add(0, record);
        return record;
    }

    public void deleteMemory(MemoryRecord record) {
        memories.remove(record);
        memoryDao.delete(record.id);
    }

    // ========== Emergency ==========

    public EmergencyAlert addEmergencyAlert(String message) {
        int id = emergencyDao.add(activeUserId, message);
        EmergencyAlert alert = new EmergencyAlert(id, now(), message, "待确认");
        emergencyAlerts.add(0, alert);
        return alert;
    }

    // ========== Search / Categories ==========

    public List<MedicineLibraryItem> searchMedicineLibrary(String keyword) {
        List<MedicineLibraryItem> result = new ArrayList<>();
        String key = keyword == null ? "" : keyword.trim();
        for (MedicineLibraryItem item : medicineLibrary) {
            if (key.isEmpty() || item.name.contains(key) || item.disease.contains(key) || item.type.contains(key)) result.add(item);
        }
        return result;
    }

    public Set<String> medicineTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add("全部类型");
        for (Medicine m : medicines) types.add(m.type);
        return types;
    }

    public Set<String> photoCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (AlbumPhoto p : photos) categories.add(p.category);
        return categories;
    }

    public Set<String> memoryCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (MemoryRecord r : memories) categories.add(r.category);
        return categories;
    }

    public Set<String> fraudCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (FraudTip t : fraudTips) categories.add(t.category);
        return categories;
    }

    // ========== Utils ==========

    public String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
