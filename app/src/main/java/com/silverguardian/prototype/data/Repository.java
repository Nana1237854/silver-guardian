package com.silverguardian.prototype.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.dao.AlbumDao;
import com.silverguardian.prototype.data.dao.ChatDao;
import com.silverguardian.prototype.data.dao.EmergencyDao;
import com.silverguardian.prototype.data.dao.FamilyDao;
import com.silverguardian.prototype.data.dao.HealthDao;
import com.silverguardian.prototype.data.dao.MedicineDao;
import com.silverguardian.prototype.data.dao.MemoryDao;
import com.silverguardian.prototype.data.dao.UserDao;
import com.silverguardian.prototype.health.HealthAlertService;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.models.BluetoothDeviceMock;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.FraudTip;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.models.MemoryRecord;
import com.silverguardian.prototype.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    private void load() {
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

        users.addAll(userDao.readAll());
        if (!findUserById(activeUserId) && !users.isEmpty()) activeUserId = users.get(0).id;

        healthData.addAll(healthDao.readAll(activeUserId));
        familyMembers.addAll(familyDao.readAll(activeUserId));
        medicines.addAll(medicineDao.readAll(activeUserId, today()));
        photos.addAll(albumDao.readAll(activeUserId));
        chatMessages.addAll(chatDao.readAll(activeUserId));
        memories.addAll(memoryDao.readAll(activeUserId));
        emergencyAlerts.addAll(emergencyDao.readAll(activeUserId));
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
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        boolean has = cursor.moveToFirst() && cursor.getInt(0) > 0;
        cursor.close();
        return has;
    }

    private boolean findUserById(int id) {
        for (User user : users) if (user.id == id) return true;
        return false;
    }

    private void seedStaticLists() {
        if (!medicineLibrary.isEmpty()) return;

        medicineLibrary.add(new MedicineLibraryItem(1, "Hypertension", "Nifedipine SR", "Adalat", "Blood Pressure", "For hypertension and angina."));
        medicineLibrary.add(new MedicineLibraryItem(2, "Hypertension", "Valsartan", "Diovan", "Blood Pressure", "For mild to moderate hypertension."));
        medicineLibrary.add(new MedicineLibraryItem(3, "Diabetes", "Metformin", "Glucophage", "Blood Sugar", "For type 2 diabetes management."));
        medicineLibrary.add(new MedicineLibraryItem(4, "Heart Disease", "Aspirin EC", "Aspirin", "Cardio", "Antiplatelet medicine."));
        medicineLibrary.add(new MedicineLibraryItem(5, "Bone Health", "Calcium D3", "Caltrate", "Vitamin", "Calcium and vitamin D supplement."));
        medicineLibrary.add(new MedicineLibraryItem(6, "Cold", "Acetaminophen", "Tylenol", "Cold Relief", "For fever and headache."));
        medicineLibrary.add(new MedicineLibraryItem(7, "Cough", "Ambroxol", "Mucosolvan", "Cough Relief", "Helps loosen mucus."));
        medicineLibrary.add(new MedicineLibraryItem(8, "Stomach Pain", "Omeprazole", "Losec", "Stomach Care", "For acid-related discomfort."));

        if (fraudTips.isEmpty()) {
            fraudTips.add(new FraudTip(1, "Fake Refund Support", "Phone Scam", "Never share codes or screen access with strangers.", "Do not share verification codes"));
            fraudTips.add(new FraudTip(2, "Health Product Seminar", "Health Scam", "Free gifts followed by expensive product sales are a common trap.", "Ask a doctor before buying"));
            fraudTips.add(new FraudTip(3, "Fake Relative Borrowing", "Family Scam", "Verify identity with a direct call before sending money.", "Call to confirm"));
            fraudTips.add(new FraudTip(4, "Prize Fee Scam", "Prize Scam", "Prizes that require a fee first are almost always fake.", "Do not transfer money"));
        }

        if (bluetoothDevices.isEmpty()) {
            bluetoothDevices.add(new BluetoothDeviceMock(1, "Blood Pressure Monitor BP-80", "blood_pressure", "126/81", "Ready"));
            bluetoothDevices.add(new BluetoothDeviceMock(2, "Pulse Oximeter OX-2", "blood_oxygen", "98", "Ready"));
            bluetoothDevices.add(new BluetoothDeviceMock(3, "Fitness Band HR-6", "heart_rate", "74", "Ready"));
        }
    }

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
    public int getActiveUserId() { return activeUserId; }

    public void setActiveUser(int userId) {
        activeUserId = userId;
        load();
    }

    public User addUser(String name, String pin, int age, String conditions) {
        int id = userDao.add(name, pin, age, conditions);
        User user = new User(id, name, pin, "", age, conditions);
        users.add(user);
        return user;
    }

    public void deleteUser(User user) {
        users.remove(user);
        userDao.delete(user.id);
        load();
    }

    public HealthData addHealthData(String type, String value, String status) {
        HealthData data = new HealthData(type, value, status, R.drawable.ic_info);
        healthData.add(0, data);
        healthDao.add(activeUserId, type, value, status);
        HealthAlertService.checkAndAlert(appContext, data);
        return data;
    }

    public void deleteHealthData(HealthData data) {
        healthData.remove(data);
    }

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

    public AlbumPhoto addPhoto(String title, String category, String message) {
        int id = albumDao.add(activeUserId, title, category, message);
        AlbumPhoto photo = new AlbumPhoto(id, "", title, "Family uploaded photo", category, "Family", false, category, message);
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

    public void addChatMessage(String text, String type) {
        chatMessages.add(new ChatMessage(text, type, now()));
        chatDao.add(activeUserId, text, type);
    }

    public void resetChatSession(String welcomeMessage) {
        chatDao.clearForUser(activeUserId);
        chatMessages.clear();
        addChatMessage(welcomeMessage, ChatMessage.TYPE_AI);
        load();
    }

    public MemoryRecord addMemory(String content, String category) {
        int id = memoryDao.add(activeUserId, content, category);
        MemoryRecord record = new MemoryRecord(id, content, category, "Just now");
        memories.add(0, record);
        return record;
    }

    public void deleteMemory(MemoryRecord record) {
        memories.remove(record);
        memoryDao.delete(record.id);
    }

    public EmergencyAlert addEmergencyAlert(String message) {
        int id = emergencyDao.add(activeUserId, message);
        EmergencyAlert alert = new EmergencyAlert(id, now(), message, "Pending");
        emergencyAlerts.add(0, alert);
        return alert;
    }

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
        types.add("ALL_TYPES");
        for (Medicine medicine : medicines) types.add(medicine.type);
        return types;
    }

    public Set<String> photoCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("ALL");
        for (AlbumPhoto photo : photos) categories.add(photo.category);
        return categories;
    }

    public Set<String> memoryCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("ALL");
        for (MemoryRecord record : memories) categories.add(record.category);
        return categories;
    }

    public Set<String> fraudCategories() {
        Set<String> categories = new LinkedHashSet<>();
        categories.add("ALL");
        for (FraudTip tip : fraudTips) categories.add(tip.category);
        return categories;
    }

    public String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
