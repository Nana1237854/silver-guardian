package com.silverguardian.prototype.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.album.PhotoStorage;
import com.silverguardian.prototype.data.dao.AlbumDao;
import com.silverguardian.prototype.data.dao.ChatDao;
import com.silverguardian.prototype.data.dao.EmergencyDao;
import com.silverguardian.prototype.data.dao.FamilyDao;
import com.silverguardian.prototype.data.dao.FraudTipDao;
import com.silverguardian.prototype.data.dao.HealthDao;
import com.silverguardian.prototype.data.dao.MedicineDao;
import com.silverguardian.prototype.data.dao.MedicineFeedbackDao;
import com.silverguardian.prototype.data.dao.MedicineLibraryDao;
import com.silverguardian.prototype.data.dao.MemoryDao;
import com.silverguardian.prototype.data.dao.SafeCheckDao;
import com.silverguardian.prototype.data.dao.UserDao;
import com.silverguardian.prototype.health.HealthAlertService;
import com.silverguardian.prototype.health.HealthMetricEvaluator;
import com.silverguardian.prototype.models.Album;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.models.BluetoothDeviceMock;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.FraudTip;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineFeedback;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.models.MemoryRecord;
import com.silverguardian.prototype.models.SafeCheckRecord;
import com.silverguardian.prototype.models.User;
import com.silverguardian.prototype.reminder.MedicineAlarmScheduler;

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
    private final MedicineFeedbackDao feedbackDao;
    private final AlbumDao albumDao;
    private final ChatDao chatDao;
    private final MemoryDao memoryDao;
    private final SafeCheckDao safeCheckDao;
    private final EmergencyDao emergencyDao;
    private final FamilyDao familyDao;
    private final MedicineLibraryDao libraryDao;
    private final FraudTipDao fraudDao;
    private final SharedPreferences session;

    private final List<User> users = new ArrayList<>();
    private final List<HealthData> healthData = new ArrayList<>();
    private final List<FamilyMember> familyMembers = new ArrayList<>();
    private final List<Medicine> medicines = new ArrayList<>();
    private final List<MedicineFeedback> medicineFeedbacks = new ArrayList<>();
    private final List<Album> albums = new ArrayList<>();
    private final List<AlbumPhoto> photos = new ArrayList<>();
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final List<MemoryRecord> memories = new ArrayList<>();
    private final List<EmergencyAlert> emergencyAlerts = new ArrayList<>();
    private final List<FraudTip> fraudTips = new ArrayList<>();
    private final List<MedicineLibraryItem> medicineLibrary = new ArrayList<>();
    private final List<BluetoothDeviceMock> bluetoothDevices = new ArrayList<>();

    private int activeUserId;

    private Repository(Context context) {
        appContext = context.getApplicationContext();
        dbHelper = new ElderlyDbHelper(appContext);
        userDao = new UserDao(dbHelper);
        healthDao = new HealthDao(dbHelper);
        medicineDao = new MedicineDao(dbHelper);
        feedbackDao = new MedicineFeedbackDao(dbHelper);
        albumDao = new AlbumDao(dbHelper);
        chatDao = new ChatDao(dbHelper);
        memoryDao = new MemoryDao(dbHelper);
        safeCheckDao = new SafeCheckDao(dbHelper);
        emergencyDao = new EmergencyDao(dbHelper);
        familyDao = new FamilyDao(dbHelper);
        libraryDao = new MedicineLibraryDao(dbHelper);
        fraudDao = new FraudTipDao(dbHelper);
        session = appContext.getSharedPreferences("elder_session", Context.MODE_PRIVATE);
        activeUserId = session.getInt("active_user_id", -1);
        seedDevices();
    }

    public static synchronized Repository init(Context context) {
        Context app = context.getApplicationContext();
        if (instance == null || instance.appContext != app) {
            if (instance != null) {
                instance.dbHelper.close();
            }
            instance = new Repository(app);
        }
        instance.load();
        return instance;
    }

    public static Repository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Repository not initialized");
        }
        return instance;
    }

    private void load() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        userDao.seedIfEmpty(db);
        users.clear();
        users.addAll(userDao.readAll());
        for (User user : users) {
            ElderlyDbHelper.seedDefaultTemplates(db, user.id);
        }
        if (!findUser(activeUserId) && !users.isEmpty()) {
            activeUserId = users.get(0).id;
        }
        if (activeUserId > 0) {
            session.edit().putInt("active_user_id", activeUserId).apply();
        }
        reloadActive();
    }

    private void reloadActive() {
        healthData.clear();
        familyMembers.clear();
        medicines.clear();
        albums.clear();
        photos.clear();
        chatMessages.clear();
        memories.clear();
        emergencyAlerts.clear();
        fraudTips.clear();
        medicineLibrary.clear();
        if (activeUserId < 0) {
            return;
        }
        healthData.addAll(healthDao.readAll(activeUserId));
        familyMembers.addAll(familyDao.readAll(activeUserId));
        medicines.addAll(medicineDao.readAll(activeUserId, today()));
        albums.addAll(albumDao.readAlbums(activeUserId));
        photos.addAll(albumDao.readAll(activeUserId));
        chatMessages.addAll(chatDao.readAll(activeUserId));
        memories.addAll(memoryDao.readAll(activeUserId));
        emergencyAlerts.addAll(emergencyDao.readAll(activeUserId));
        fraudTips.addAll(fraudDao.readAll(activeUserId));
        medicineLibrary.addAll(libraryDao.search(activeUserId, ""));
        medicineFeedbacks.clear();
        medicineFeedbacks.addAll(feedbackDao.readAll(activeUserId));
    }

    private boolean findUser(int id) {
        for (User user : users) {
            if (user.id == id) {
                return true;
            }
        }
        return false;
    }

    private void seedDevices() {
        if (!bluetoothDevices.isEmpty()) {
            return;
        }
        bluetoothDevices.add(new BluetoothDeviceMock(1, "Blood Pressure Monitor BP-80", "blood_pressure", "126/81", "Ready"));
        bluetoothDevices.add(new BluetoothDeviceMock(2, "Pulse Oximeter OX-2", "blood_oxygen", "98", "Ready"));
        bluetoothDevices.add(new BluetoothDeviceMock(3, "Fitness Band HR-6", "heart_rate", "74", "Ready"));
    }

    public List<User> getUsers() { return users; }
    public List<HealthData> getHealthData() { return healthData; }
    public List<HealthData> getTodayHealthData() { return healthDao.readToday(activeUserId); }
    public List<FamilyMember> getFamilyMembers() { return familyMembers; }
    public List<Medicine> getMedicines() { return medicines; }
    public List<MedicineFeedback> getTodayMedicineFeedbacks() {
        return feedbackDao.readByDate(activeUserId, today());
    }
    public int getTodayMedicineFeedbackWarningCount() {
        return feedbackDao.countWarningsByDate(activeUserId, today());
    }
    public List<MedicineFeedback> getTodayMedicineFeedbackWarnings() {
        return feedbackDao.readWarningsByDate(activeUserId, today());
    }
    public MedicineFeedback addMedicineFeedback(int medicineId, String medicineName, String feedbackType, String feedbackText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String date = today();
        String createdAt = nowDateTime();
        String safeName = (medicineName == null || medicineName.trim().isEmpty()) ? "未知药品" : medicineName.trim();
        String safeType = (feedbackType == null || feedbackType.trim().isEmpty()) ? MedicineFeedback.TYPE_SKIPPED : feedbackType;
        String safeText = feedbackText == null ? "" : feedbackText;
        long id = feedbackDao.add(db, activeUserId, medicineId, safeName, safeType, safeText, date, createdAt);
        MedicineFeedback feedback = new MedicineFeedback((int) id, activeUserId, medicineId, safeName, safeType, safeText, createdAt, date);
        medicineFeedbacks.add(0, feedback);
        return feedback;
    }
    public List<Album> getAlbums() { return albums; }
    public List<AlbumPhoto> getPhotos() { return photos; }
    public List<ChatMessage> getWelcomeMessages() { return chatMessages; }
    public List<MemoryRecord> getMemories() { return memories; }
    public List<FraudTip> getFraudTips() { return fraudTips; }
    public List<EmergencyAlert> getEmergencyAlerts() { return emergencyAlerts; }
    public SafeCheckRecord getTodaySafeCheckRecord() { return getTodaySafeCheckRecord(activeUserId); }
    public SafeCheckRecord getTodaySafeCheckRecord(int userId) {
        if (userId <= 0) {
            return null;
        }
        return safeCheckDao.readToday(userId, today());
    }
    public List<MedicineLibraryItem> getMedicineLibrary() { return medicineLibrary; }
    public List<BluetoothDeviceMock> getBluetoothDevices() { return bluetoothDevices; }
    public int getActiveUserId() { return activeUserId; }
    public void setActiveUser(int id) {
        if (!findUser(id)) {
            return;
        }
        activeUserId = id;
        session.edit().putInt("active_user_id", id).apply();
        reloadActive();
    }

    public User addUser(String name, String pin, int age, String conditions) {
        return addUser(name, pin, age, conditions, "", "");
    }

    public User addUser(String name, String pin, int age, String conditions, String question, String answer) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int id;
        db.beginTransaction();
        try {
            id = userDao.add(db, name, pin, age, conditions, question, answer);
            ElderlyDbHelper.seedDefaultTemplates(db, id);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        load();
        for (User user : users) {
            if (user.id == id) {
                return user;
            }
        }
        throw new IllegalStateException("User insert failed");
    }

    public void deleteUser(User user) {
        userDao.delete(user.id);
        load();
    }

    public FamilyMember addFamilyMember(String name, String relationship, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = familyDao.add(db, activeUserId, name, relationship, phone);
        FamilyMember member = new FamilyMember((int) id, name, relationship, phone, "", false);
        familyMembers.add(member);
        return member;
    }

    public void updateFamilyMember(FamilyMember member, String name, String relationship, String phone) {
        if (member == null) {
            return;
        }
        familyDao.update(dbHelper.getWritableDatabase(), member.id, name, relationship, phone);
        member.name = name;
        member.relationship = relationship;
        member.phone = phone;
    }

    public void deleteFamilyMember(FamilyMember member) {
        if (member == null) {
            return;
        }
        familyDao.delete(dbHelper.getWritableDatabase(), member.id);
        familyMembers.remove(member);
    }

    public HealthData addHealthData(String type, String value, String status) {
        return addHealthData(type, value, status, "");
    }

    public HealthData addHealthData(String type, String value, String status, String notes) {
        String finalStatus = status == null || status.isEmpty() ? HealthMetricEvaluator.status(type, value) : status;
        int id = healthDao.add(activeUserId, type, value, finalStatus, notes);
        HealthData item = new HealthData(id, type, value, finalStatus, notes, nowDateTime(), R.drawable.ic_info);
        healthData.add(0, item);
        HealthAlertService.checkAndAlert(appContext, item);
        return item;
    }

    public Medicine addMedicine(String name, String type, String time, String method, String desc) {
        return addMedicine(name, type, time, method, desc, 0, 0, 10);
    }

    public Medicine addMedicine(String name, String type, String time, String method, String desc, int advance, int repeats, int interval) {
        int id = medicineDao.add(activeUserId, name, type, time, method, desc, advance, repeats, interval);
        Medicine medicine = new Medicine(id, name, type, time, method, desc, "", false, advance, repeats, interval);
        medicines.add(0, medicine);
        MedicineAlarmScheduler.schedule(appContext, activeUserId, activeUserName(), medicine);
        return medicine;
    }

    public void deleteMedicine(Medicine medicine) {
        MedicineAlarmScheduler.cancel(appContext, activeUserId, medicine);
        medicineDao.delete(medicine.id);
        medicines.remove(medicine);
    }

    public void toggleMedicineTaken(Medicine medicine, boolean taken) {
        medicineDao.setTaken(activeUserId, medicine.id, taken, today());
        medicine.takenToday = taken;
    }

    public SafeCheckRecord saveTodaySafeCheckStatus(String status, String note) {
        return saveTodaySafeCheckStatus(activeUserId, status, note);
    }

    public SafeCheckRecord saveTodaySafeCheckStatus(int userId, String status, String note) {
        if (userId <= 0) {
            throw new IllegalStateException("No active user for safe check");
        }
        return safeCheckDao.upsertToday(userId, status, note, nowDateTime(), today());
    }

    public boolean isMedicineTakenToday(int userId, int medicineId) {
        return medicineDao.isTakenToday(userId, medicineId, today());
    }

    public Album addAlbum(String name) {
        Album album = findOrCreateAlbum(name);
        Album existing = findAlbumById(album.id);
        return existing == null ? album : existing;
    }

    public void deleteAlbum(Album album) {
        if (album == null) {
            return;
        }
        List<AlbumPhoto> photosToDelete = getPhotosForAlbum(album);
        for (AlbumPhoto photo : photosToDelete) {
            PhotoStorage.delete(appContext, photo.url, photo.publicUri);
        }
        albumDao.deleteAlbum(album.id);
        albums.remove(album);
        photos.removeAll(photosToDelete);
        reloadAlbumsOnly();
    }

    public List<AlbumPhoto> getPhotosForAlbum(Album album) {
        if (album == null) {
            return new ArrayList<>(photos);
        }
        return albumDao.readPhotosForAlbum(activeUserId, album.id, album.name);
    }

    public AlbumPhoto addPhoto(int albumId, String privatePath, String publicUri, String title, String category, String message) {
        Album targetAlbum = findAlbumById(albumId);
        String albumName = category;
        if (targetAlbum != null) {
            albumName = targetAlbum.name;
        } else if (albumName == null || albumName.trim().isEmpty()) {
            albumName = "Other";
        }
        int id = albumDao.addPhoto(activeUserId, albumId, privatePath, publicUri, title, albumName, message);
        AlbumPhoto photo = new AlbumPhoto(id, albumId, privatePath, publicUri, title, "Family uploaded photo", albumName, "Family", false, albumName, message);
        photos.add(0, photo);
        reloadAlbumsOnly();
        return photo;
    }

    public AlbumPhoto addPhoto(String title, String category, String message) {
        return addPhoto("", "", title, category, message);
    }

    public AlbumPhoto addPhoto(String privatePath, String publicUri, String title, String category, String message) {
        Album album = findOrCreateAlbum(category);
        return addPhoto(album.id, privatePath, publicUri, title, album.name, message);
    }

    public void deletePhoto(AlbumPhoto photo) {
        PhotoStorage.delete(appContext, photo.url, photo.publicUri);
        albumDao.delete(photo.id);
        photos.remove(photo);
        reloadAlbumsOnly();
    }

    public void toggleFavorite(AlbumPhoto photo) {
        photo.favorite = !photo.favorite;
        albumDao.setFavorite(photo.id, photo.favorite);
    }
    public void addChatMessage(String text, String type) {
        chatMessages.add(new ChatMessage(text, type, now()));
        chatDao.add(activeUserId, text, type);
    }

    public void resetChatSession(String welcome) {
        chatDao.clearForUser(activeUserId);
        chatMessages.clear();
        addChatMessage(welcome, ChatMessage.TYPE_AI);
    }

    public MemoryRecord addMemory(String content, String category) {
        int id = memoryDao.add(activeUserId, content, category);
        MemoryRecord record = new MemoryRecord(id, content, category, "Just now");
        memories.add(0, record);
        return record;
    }

    public void deleteMemory(MemoryRecord record) {
        memoryDao.delete(record.id);
        memories.remove(record);
    }

    public EmergencyAlert addEmergencyAlert(String message) {
        return addEmergencyAlertForUser(activeUserId, message);
    }

    public EmergencyAlert addEmergencyAlertForUser(int userId, String message) {
        int id = emergencyDao.add(userId, message);
        EmergencyAlert alert = new EmergencyAlert(id, nowDateTime(), message, "Pending");
        if (userId == activeUserId) {
            emergencyAlerts.add(0, alert);
        }
        return alert;
    }

    public List<MedicineLibraryItem> searchMedicineLibrary(String keyword) {
        return libraryDao.search(activeUserId, keyword);
    }

    public void addLibraryItem(MedicineLibraryItem item) {
        libraryDao.upsert(activeUserId, item);
        medicineLibrary.clear();
        medicineLibrary.addAll(libraryDao.search(activeUserId, ""));
    }

    public void updateLibraryItem(MedicineLibraryItem item) {
        libraryDao.update(activeUserId, item);
        reloadActive();
    }

    public void deleteLibraryItem(MedicineLibraryItem item) {
        libraryDao.delete(activeUserId, item.id);
        reloadActive();
    }

    public Set<String> medicineTypes() {
        Set<String> values = new LinkedHashSet<>();
        values.add("ALL_TYPES");
        for (Medicine medicine : medicines) {
            values.add(medicine.type);
        }
        return values;
    }

    public Set<String> photoCategories() {
        Set<String> values = new LinkedHashSet<>();
        values.add("ALL");
        for (AlbumPhoto photo : photos) {
            values.add(photo.category);
        }
        return values;
    }

    public Set<String> memoryCategories() {
        Set<String> values = new LinkedHashSet<>();
        values.add("ALL");
        for (MemoryRecord memory : memories) {
            values.add(memory.category);
        }
        return values;
    }

    public Set<String> fraudCategories() {
        Set<String> values = new LinkedHashSet<>();
        values.add("ALL");
        for (FraudTip tip : fraudTips) {
            values.add(tip.category);
        }
        return values;
    }

    private Album findOrCreateAlbum(String name) {
        String safeName = name == null || name.trim().isEmpty() ? "Other" : name.trim();
        for (Album album : albums) {
            if (safeName.equals(album.name)) {
                return album;
            }
        }
        int albumId = albumDao.createAlbum(activeUserId, safeName);
        reloadAlbumsOnly();
        Album created = findAlbumById(albumId);
        if (created != null) {
            return created;
        }
        Album fallback = new Album(albumId, safeName, "", 0, nowDateTime());
        albums.add(0, fallback);
        return fallback;
    }

    private Album findAlbumById(int albumId) {
        for (Album album : albums) {
            if (album.id == albumId) {
                return album;
            }
        }
        return null;
    }

    private void reloadAlbumsOnly() {
        albums.clear();
        if (activeUserId >= 0) {
            albums.addAll(albumDao.readAlbums(activeUserId));
        }
    }

    private String activeUserName() {
        for (User user : users) {
            if (user.id == activeUserId) {
                return user.name;
            }
        }
        return "Elder";
    }

    public String getUserName(int userId) {
        for (User user : users) {
            if (user.id == userId) {
                return user.name;
            }
        }
        return "Elder";
    }

    public int getTodayEmergencyAlertCount() {
        int count = 0;
        String date = today();
        for (int i = 0; i < emergencyAlerts.size(); i++) {
            EmergencyAlert alert = emergencyAlerts.get(i);
            if (alert.time != null && alert.time.startsWith(date)) {
                count++;
            }
        }
        return count;
    }

    public int getTodayPhotoCount() {
        if (activeUserId <= 0) {
            return 0;
        }
        return albumDao.readTodayCount(activeUserId);
    }

    public String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private String nowDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}


