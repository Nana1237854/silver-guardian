package com.silverguardian.prototype.data;

import android.content.Context;

import com.silverguardian.prototype.models.*;

import java.util.List;
import java.util.Set;

/**
 * 向后兼容的静态 API 层。全部委托给 {@link Repository}。
 * 新代码应直接使用 Repository.getInstance()。
 *
 * @deprecated 请使用 {@link Repository}
 */
@Deprecated
public class MockData {

    public static void init(Context context) {
        Repository.init(context);
    }

    private static Repository repo() {
        return Repository.getInstance();
    }

    // ========== Getters ==========

    public static List<User> getUsers() { return repo().getUsers(); }
    public static List<HealthData> getHealthData() { return repo().getHealthData(); }
    public static List<FamilyMember> getFamilyMembers() { return repo().getFamilyMembers(); }
    public static List<Medicine> getMedicines() { return repo().getMedicines(); }
    public static List<AlbumPhoto> getPhotos() { return repo().getPhotos(); }
    public static List<ChatMessage> getWelcomeMessages() { return repo().getWelcomeMessages(); }
    public static List<MemoryRecord> getMemories() { return repo().getMemories(); }
    public static List<FraudTip> getFraudTips() { return repo().getFraudTips(); }
    public static List<EmergencyAlert> getEmergencyAlerts() { return repo().getEmergencyAlerts(); }
    public static List<MedicineLibraryItem> getMedicineLibrary() { return repo().getMedicineLibrary(); }
    public static List<BluetoothDeviceMock> getBluetoothDevices() { return repo().getBluetoothDevices(); }

    // ========== Active User ==========

    public static void setActiveUser(int userId) { repo().setActiveUser(userId); }

    // ========== User CRUD ==========

    public static User addUser(String name, String pin, int age, String conditions) {
        return repo().addUser(name, pin, age, conditions);
    }

    public static void deleteUser(User user) { repo().deleteUser(user); }

    // ========== Health CRUD ==========

    public static HealthData addHealthData(String type, String value, String status) {
        return repo().addHealthData(type, value, status);
    }

    public static void deleteHealthData(HealthData data) { repo().deleteHealthData(data); }

    // ========== Medicine CRUD ==========

    public static Medicine addMedicine(String name, String type, String time, String method, String description) {
        return repo().addMedicine(name, type, time, method, description);
    }

    public static void deleteMedicine(Medicine medicine) { repo().deleteMedicine(medicine); }

    public static void toggleMedicineTaken(Medicine medicine, boolean taken) { repo().toggleMedicineTaken(medicine, taken); }

    // ========== Album CRUD ==========

    public static AlbumPhoto addPhoto(String title, String category, String message) {
        return repo().addPhoto(title, category, message);
    }

    public static void deletePhoto(AlbumPhoto photo) { repo().deletePhoto(photo); }

    public static void toggleFavorite(AlbumPhoto photo) { repo().toggleFavorite(photo); }

    // ========== Chat ==========

    public static void addChatMessage(String text, String type) { repo().addChatMessage(text, type); }

    // ========== Memory CRUD ==========

    public static MemoryRecord addMemory(String content, String category) {
        return repo().addMemory(content, category);
    }

    public static void deleteMemory(MemoryRecord record) { repo().deleteMemory(record); }

    // ========== Emergency ==========

    public static EmergencyAlert addEmergencyAlert(String message) {
        return repo().addEmergencyAlert(message);
    }

    // ========== Search / Categories ==========

    public static List<MedicineLibraryItem> searchMedicineLibrary(String keyword) {
        return repo().searchMedicineLibrary(keyword);
    }

    public static Set<String> medicineTypes() { return repo().medicineTypes(); }
    public static Set<String> photoCategories() { return repo().photoCategories(); }
    public static Set<String> memoryCategories() { return repo().memoryCategories(); }
    public static Set<String> fraudCategories() { return repo().fraudCategories(); }

    // ========== Utils ==========

    public static String now() { return repo().now(); }
}
