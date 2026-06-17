package com.silverguardian.prototype.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
// ElderlyDbHelper extracted to ElderlyDbHelper.java

import com.silverguardian.prototype.models.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MockData {
    private static ElderlyDbHelper dbHelper;
    private static final List<User> users = new ArrayList<>();
    private static final List<HealthData> healthData = new ArrayList<>();
    private static final List<FamilyMember> familyMembers = new ArrayList<>();
    private static final List<Medicine> medicines = new ArrayList<>();
    private static final List<AlbumPhoto> photos = new ArrayList<>();
    private static final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final List<MemoryRecord> memories = new ArrayList<>();
    private static final List<FraudTip> fraudTips = new ArrayList<>();
    private static final List<EmergencyAlert> emergencyAlerts = new ArrayList<>();
    private static final List<MedicineLibraryItem> medicineLibrary = new ArrayList<>();
    private static final List<BluetoothDeviceMock> bluetoothDevices = new ArrayList<>();
    private static boolean loaded = false;
    private static int activeUserId = 1;

    public static synchronized void init(Context context) {
        if (dbHelper == null) {
            dbHelper = new ElderlyDbHelper(context.getApplicationContext());
        }
        load();
    }

    private static void requireLoaded() {
        if (!loaded) {
            seedStaticLists();
            loaded = true;
        }
    }

    private static void load() {
        seedStaticLists();
        users.clear();
        healthData.clear();
        familyMembers.clear();
        medicines.clear();
        photos.clear();
        chatMessages.clear();
        memories.clear();
        emergencyAlerts.clear();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        seedDatabaseIfEmpty(db);
        int requestedUserId = activeUserId;
        readUsers(db);
        boolean foundRequestedUser = false;
        for (User user : users) {
            if (user.id == requestedUserId) {
                foundRequestedUser = true;
                break;
            }
        }
        if (!foundRequestedUser && !users.isEmpty()) activeUserId = users.get(0).id;
        readHealth(db);
        readFamily(db);
        readMedicines(db);
        readPhotos(db);
        readMessages(db);
        readMemories(db);
        readEmergencyAlerts(db);
        loaded = true;
    }

    private static void seedStaticLists() {
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

    private static void seedDatabaseIfEmpty(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        boolean empty = true;
        if (cursor.moveToFirst()) empty = cursor.getInt(0) == 0;
        cursor.close();
        if (!empty) return;

        long userId = insert(db, "users", values(
            "name", "颜爷爷", "pin", "1234", "avatar", "", "age", 72, "health_conditions", "高血压、冠心病"
        ));
        insert(db, "users", values("name", "林奶奶", "pin", "5678", "avatar", "", "age", 68, "health_conditions", "糖尿病、骨质疏松"));

        insertHealth(db, userId, "sleep", "5小时14分钟", "注意");
        insertHealth(db, userId, "exercise", "206千卡", "良好");
        insertHealth(db, userId, "mood", "良好", "今天 20:10 记录");
        insertHealth(db, userId, "steps", "2265/10000", "22%");
        insertHealth(db, userId, "breathing", "0/3", "分钟");
        insertHealth(db, userId, "heart_rate", "48", "偏低");
        insertHealth(db, userId, "blood_pressure", "128/82", "正常");

        insert(db, "family_members", values("user_id", userId, "name", "大明", "relationship", "儿子", "phone", "13800001111", "avatar", "", "status", "online"));
        insert(db, "family_members", values("user_id", userId, "name", "小柔", "relationship", "女儿", "phone", "13800002222", "avatar", "", "status", "online"));

        insertMedicine(db, userId, "硝苯地平缓释片", "降压药", "08:00,20:00", "口服", "餐后服用，每次 1 片", true);
        insertMedicine(db, userId, "阿托伐他汀钙片 10mg", "降血脂药", "20:00", "口服", "睡前服用", false);
        insertMedicine(db, userId, "维生素D3胶囊 1000IU", "维生素", "12:00", "口服", "随餐服用", true);
        insertMedicine(db, userId, "碳酸钙D3片", "钙片", "16:00", "口服", "随餐服用", false);

        insert(db, "album_photos", values("user_id", userId, "url", "", "title", "春节团聚", "description", "2024 年春节全家福", "category", "家庭", "uploaded_by", "小明", "favorite", 1, "scene_tag", "春节团聚", "family_message", "祝爷爷奶奶身体健康！"));
        insert(db, "album_photos", values("user_id", userId, "url", "", "title", "公园散步", "description", "春天一起去公园", "category", "日常", "uploaded_by", "小柔", "favorite", 0, "scene_tag", "户外活动", "family_message", "天气好要多出去走走"));

        insert(db, "messages", values("user_id", userId, "content", "你好！我是银发守护助手，有健康问题、用药疑问或生活困扰，随时告诉我。", "type", "ai"));
        insert(db, "memories", values("user_id", userId, "memory", "喜欢每天傍晚去小区花园散步。", "category", "爱好"));
        insert(db, "emergency_alerts", values("user_id", userId, "keyword", "SOS", "message", "今天 09:12 模拟 SOS 求助已发送给大明、小柔。"));
    }

    private static ContentValues values(Object... pairs) {
        ContentValues values = new ContentValues();
        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Object value = pairs[i + 1];
            if (value instanceof Integer) values.put(key, (Integer) value);
            else if (value instanceof Long) values.put(key, (Long) value);
            else values.put(key, String.valueOf(value));
        }
        return values;
    }

    private static long insert(SQLiteDatabase db, String table, ContentValues values) {
        return db.insert(table, null, values);
    }

    private static void insertHealth(SQLiteDatabase db, long userId, String type, String value, String status) {
        insert(db, "health_data", values("user_id", userId, "type", type, "value", value, "notes", status));
    }

    private static void insertMedicine(SQLiteDatabase db, long userId, String name, String type, String time, String method, String description, boolean taken) {
        long medId = insert(db, "user_medicines", values("user_id", userId, "name", name, "type", type, "time", time, "method", method, "description", description, "image", ""));
        if (taken) insert(db, "medicine_taken", values("user_id", userId, "medicine_id", medId, "taken_date", today()));
    }

    private static void readUsers(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id,name,pin,avatar,age,health_conditions FROM users ORDER BY id", null);
        while (c.moveToNext()) users.add(new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5)));
        c.close();
    }

    private static void readHealth(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT type,value,COALESCE(notes,'正常') FROM health_data WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) healthData.add(new HealthData(c.getString(0), c.getString(1), c.getString(2), android.R.drawable.ic_menu_info_details));
        c.close();
    }

    private static void readFamily(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id,name,relationship,phone,avatar,status FROM family_members WHERE user_id=? ORDER BY id", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) familyMembers.add(new FamilyMember(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), "online".equals(c.getString(5))));
        c.close();
    }

    private static void readMedicines(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT m.id,m.name,m.type,m.time,m.method,m.description,m.image,CASE WHEN t.id IS NULL THEN 0 ELSE 1 END FROM user_medicines m LEFT JOIN medicine_taken t ON t.medicine_id=m.id AND t.taken_date=? WHERE m.user_id=? ORDER BY m.id", new String[]{today(), String.valueOf(activeUserId)});
        while (c.moveToNext()) medicines.add(new Medicine(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getInt(7) == 1));
        c.close();
    }

    private static void readPhotos(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id,url,title,description,category,uploaded_by,favorite,scene_tag,family_message FROM album_photos WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) photos.add(new AlbumPhoto(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getInt(6) == 1, c.getString(7), c.getString(8)));
        c.close();
    }

    private static void readMessages(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT content,type,strftime('%H:%M',created_at) FROM messages WHERE user_id=? ORDER BY id", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) chatMessages.add(new ChatMessage(c.getString(0), c.getString(1), c.getString(2) == null ? now() : c.getString(2)));
        c.close();
    }

    private static void readMemories(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id,memory,category FROM memories WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) memories.add(new MemoryRecord(c.getInt(0), c.getString(1), c.getString(2), "最近"));
        c.close();
    }

    private static void readEmergencyAlerts(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT id,created_at,message FROM emergency_alerts WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(activeUserId)});
        while (c.moveToNext()) emergencyAlerts.add(new EmergencyAlert(c.getInt(0), "今天", c.getString(2), "已响应"));
        c.close();
    }

    public static List<User> getUsers() { requireLoaded(); return users; }
    public static List<HealthData> getHealthData() { requireLoaded(); return healthData; }
    public static List<FamilyMember> getFamilyMembers() { requireLoaded(); return familyMembers; }
    public static List<Medicine> getMedicines() { requireLoaded(); return medicines; }
    public static List<AlbumPhoto> getPhotos() { requireLoaded(); return photos; }
    public static List<ChatMessage> getWelcomeMessages() { requireLoaded(); return chatMessages; }
    public static List<MemoryRecord> getMemories() { requireLoaded(); return memories; }
    public static List<FraudTip> getFraudTips() { requireLoaded(); return fraudTips; }
    public static List<EmergencyAlert> getEmergencyAlerts() { requireLoaded(); return emergencyAlerts; }
    public static List<MedicineLibraryItem> getMedicineLibrary() { requireLoaded(); return medicineLibrary; }
    public static List<BluetoothDeviceMock> getBluetoothDevices() { requireLoaded(); return bluetoothDevices; }

    public static void setActiveUser(int userId) {
        activeUserId = userId;
        if (dbHelper != null) load();
    }

    public static User addUser(String name, String pin, int age, String conditions) {
        requireLoaded();
        int id = users.isEmpty() ? 1 : users.get(users.size() - 1).id + 1;
        if (dbHelper != null) {
            id = (int) insert(dbHelper.getWritableDatabase(), "users", values("name", name, "pin", pin, "avatar", "", "age", age, "health_conditions", conditions));
        }
        User user = new User(id, name, pin, "", age, conditions);
        users.add(user);
        return user;
    }

    public static void deleteUser(User user) {
        requireLoaded();
        users.remove(user);
        if (dbHelper != null) dbHelper.getWritableDatabase().delete("users", "id=?", new String[]{String.valueOf(user.id)});
    }

    public static HealthData addHealthData(String type, String value, String status) {
        requireLoaded();
        HealthData data = new HealthData(type, value, status, android.R.drawable.ic_menu_info_details);
        healthData.add(0, data);
        if (dbHelper != null) insertHealth(dbHelper.getWritableDatabase(), activeUserId, type, value, status);
        return data;
    }

    public static void deleteHealthData(HealthData data) {
        requireLoaded();
        healthData.remove(data);
    }

    public static Medicine addMedicine(String name, String type, String time, String method, String description) {
        requireLoaded();
        int id = medicines.isEmpty() ? 1 : medicines.get(medicines.size() - 1).id + 1;
        if (dbHelper != null) {
            id = (int) insert(dbHelper.getWritableDatabase(), "user_medicines", values("user_id", activeUserId, "name", name, "type", type, "time", time, "method", method, "description", description, "image", ""));
        }
        Medicine medicine = new Medicine(id, name, type, time, method, description, "", false);
        medicines.add(0, medicine);
        return medicine;
    }

    public static void deleteMedicine(Medicine medicine) {
        requireLoaded();
        medicines.remove(medicine);
        if (dbHelper != null) dbHelper.getWritableDatabase().delete("user_medicines", "id=?", new String[]{String.valueOf(medicine.id)});
    }

    public static void toggleMedicineTaken(Medicine medicine, boolean taken) {
        requireLoaded();
        medicine.takenToday = taken;
        if (dbHelper == null) return;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (taken) {
            insert(db, "medicine_taken", values("user_id", activeUserId, "medicine_id", medicine.id, "taken_date", today()));
        } else {
            db.delete("medicine_taken", "user_id=? AND medicine_id=? AND taken_date=?", new String[]{String.valueOf(activeUserId), String.valueOf(medicine.id), today()});
        }
    }

    public static AlbumPhoto addPhoto(String title, String category, String message) {
        requireLoaded();
        int id = photos.isEmpty() ? 1 : photos.get(0).id + 1;
        if (dbHelper != null) {
            id = (int) insert(dbHelper.getWritableDatabase(), "album_photos", values("user_id", activeUserId, "url", "", "title", title, "description", "家属上传照片", "category", category, "uploaded_by", "家属", "favorite", 0, "scene_tag", category, "family_message", message));
        }
        AlbumPhoto photo = new AlbumPhoto(id, "", title, "家属上传照片", category, "家属", false, category, message);
        photos.add(0, photo);
        return photo;
    }

    public static void deletePhoto(AlbumPhoto photo) {
        requireLoaded();
        photos.remove(photo);
        if (dbHelper != null) dbHelper.getWritableDatabase().delete("album_photos", "id=?", new String[]{String.valueOf(photo.id)});
    }

    public static void toggleFavorite(AlbumPhoto photo) {
        requireLoaded();
        photo.favorite = !photo.favorite;
        if (dbHelper != null) {
            ContentValues v = new ContentValues();
            v.put("favorite", photo.favorite ? 1 : 0);
            dbHelper.getWritableDatabase().update("album_photos", v, "id=?", new String[]{String.valueOf(photo.id)});
        }
    }

    public static MemoryRecord addMemory(String content, String category) {
        requireLoaded();
        int id = memories.isEmpty() ? 1 : memories.get(0).id + 1;
        if (dbHelper != null) id = (int) insert(dbHelper.getWritableDatabase(), "memories", values("user_id", activeUserId, "memory", content, "category", category));
        MemoryRecord record = new MemoryRecord(id, content, category, "刚刚");
        memories.add(0, record);
        return record;
    }

    public static void deleteMemory(MemoryRecord record) {
        requireLoaded();
        memories.remove(record);
        if (dbHelper != null) dbHelper.getWritableDatabase().delete("memories", "id=?", new String[]{String.valueOf(record.id)});
    }

    public static EmergencyAlert addEmergencyAlert(String message) {
        requireLoaded();
        int id = emergencyAlerts.isEmpty() ? 1 : emergencyAlerts.get(0).id + 1;
        if (dbHelper != null) id = (int) insert(dbHelper.getWritableDatabase(), "emergency_alerts", values("user_id", activeUserId, "keyword", "SOS", "message", message));
        EmergencyAlert alert = new EmergencyAlert(id, now(), message, "待确认");
        emergencyAlerts.add(0, alert);
        return alert;
    }

    public static void addChatMessage(String text, String type) {
        requireLoaded();
        chatMessages.add(new ChatMessage(text, type, now()));
        if (dbHelper != null) insert(dbHelper.getWritableDatabase(), "messages", values("user_id", activeUserId, "content", text, "type", type));
    }

    public static List<MedicineLibraryItem> searchMedicineLibrary(String keyword) {
        requireLoaded();
        List<MedicineLibraryItem> result = new ArrayList<>();
        String key = keyword == null ? "" : keyword.trim();
        for (MedicineLibraryItem item : medicineLibrary) {
            if (key.isEmpty() || item.name.contains(key) || item.disease.contains(key) || item.type.contains(key)) result.add(item);
        }
        return result;
    }

    public static Set<String> medicineTypes() {
        requireLoaded();
        Set<String> types = new LinkedHashSet<>();
        types.add("全部类型");
        for (Medicine medicine : medicines) types.add(medicine.type);
        return types;
    }

    public static Set<String> photoCategories() {
        requireLoaded();
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (AlbumPhoto photo : photos) categories.add(photo.category);
        return categories;
    }

    public static Set<String> memoryCategories() {
        requireLoaded();
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (MemoryRecord record : memories) categories.add(record.category);
        return categories;
    }

    public static Set<String> fraudCategories() {
        requireLoaded();
        Set<String> categories = new LinkedHashSet<>();
        categories.add("全部");
        for (FraudTip tip : fraudTips) categories.add(tip.category);
        return categories;
    }

    public static String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
