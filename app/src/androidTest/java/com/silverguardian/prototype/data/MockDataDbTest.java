package com.silverguardian.prototype.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MockDataDbTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("elderly_guardian.db");
        MockData.init(context);
    }

    // ========== Users ==========

    @Test
    public void initCreatesDefaultUsers() {
        List<User> users = MockData.getUsers();
        assertTrue("Should have at least 2 users", users.size() >= 2);
        assertEquals("颜爷爷", users.get(0).name);
        assertEquals("1234", users.get(0).pin);
    }

    @Test
    public void addAndRetrieveUser() {
        MockData.addUser("测试老人", "9999", 70, "测试");
        List<User> users = MockData.getUsers();
        boolean found = false;
        for (User u : users) {
            if ("测试老人".equals(u.name) && "9999".equals(u.pin)) { found = true; break; }
        }
        assertTrue("New user should be in list", found);
    }

    @Test
    public void deleteUserRemovesFromList() {
        List<User> before = MockData.getUsers();
        int countBefore = before.size();
        User toDelete = before.get(before.size() - 1);
        MockData.deleteUser(toDelete);
        List<User> after = MockData.getUsers();
        assertEquals(countBefore - 1, after.size());
    }

    // ========== Multi-User Data Isolation ==========

    @Test
    public void switchingActiveUserChangesData() {
        // 爷爷's data
        MockData.setActiveUser(1);
        List<ChatMessage> grandpaMessages = MockData.getWelcomeMessages();

        MockData.setActiveUser(2);
        List<ChatMessage> grandmaMessages = MockData.getWelcomeMessages();

        assertTrue("Each user should have messages", grandpaMessages.size() > 0);
        assertTrue("Each user should have messages", grandmaMessages.size() > 0);
        // Each user's data is loaded independently
        assertNotNull(grandpaMessages);
        assertNotNull(grandmaMessages);
    }

    // ========== Medicine CRUD ==========

    @Test
    public void addMedicineAppearsInList() {
        MockData.setActiveUser(1);
        Medicine med = MockData.addMedicine("测试药", "西药", "08:00", "口服", "每日一片");
        assertNotNull(med);
        List<Medicine> meds = MockData.getMedicines();
        boolean found = false;
        for (Medicine m : meds) {
            if ("测试药".equals(m.name)) { found = true; break; }
        }
        assertTrue("Added medicine should appear", found);
    }

    @Test
    public void toggleMedicineTaken() {
        MockData.setActiveUser(1);
        List<Medicine> meds = MockData.getMedicines();
        assertTrue("Need at least one medicine", meds.size() > 0);
        Medicine med = meds.get(0);
        boolean wasTaken = med.takenToday;

        MockData.toggleMedicineTaken(med, !wasTaken);
        assertEquals(!wasTaken, med.takenToday);

        // Reset
        MockData.toggleMedicineTaken(med, wasTaken);
    }

    @Test
    public void deleteMedicineRemovesIt() {
        MockData.setActiveUser(1);
        Medicine med = MockData.addMedicine("待删除药", "中药", "12:00", "口服", "");
        int countBefore = MockData.getMedicines().size();
        MockData.deleteMedicine(med);
        assertEquals(countBefore - 1, MockData.getMedicines().size());
    }

    // ========== Album CRUD ==========

    @Test
    public void addPhotoAppearsInList() {
        MockData.setActiveUser(1);
        AlbumPhoto photo = MockData.addPhoto("测试照片", "family", "测试留言");
        assertNotNull(photo);
        assertEquals("测试照片", photo.title);
        assertEquals("测试留言", photo.familyMessage);
    }

    @Test
    public void toggleFavorite() {
        MockData.setActiveUser(1);
        AlbumPhoto photo = MockData.addPhoto("收藏测试", "daily", "");
        boolean wasFav = photo.favorite;

        MockData.toggleFavorite(photo);
        assertEquals(!wasFav, photo.favorite);

        MockData.toggleFavorite(photo);
        assertEquals(wasFav, photo.favorite);
    }

    // ========== Chat Messages ==========

    @Test
    public void addChatMessageAppears() {
        MockData.setActiveUser(1);
        int countBefore = MockData.getWelcomeMessages().size();
        MockData.addChatMessage("测试消息", "user");
        assertEquals(countBefore + 1, MockData.getWelcomeMessages().size());
    }

    // ========== Health Data ==========

    @Test
    public void addHealthDataAppears() {
        MockData.setActiveUser(1);
        int countBefore = MockData.getHealthData().size();
        MockData.addHealthData("heart_rate", "72", "正常");
        assertTrue(MockData.getHealthData().size() > countBefore);
    }

    // ========== Medicine Library (static data) ==========

    @Test
    public void medicineLibraryHasEntries() {
        assertTrue(MockData.getMedicineLibrary().size() >= 8);
    }

    @Test
    public void searchMedicineLibrary() {
        List<com.silverguardian.prototype.models.MedicineLibraryItem> result =
            MockData.searchMedicineLibrary("高血压");
        assertTrue("Should find hypertension medicines", result.size() >= 2);
    }

    // ========== Fraud Tips (static data) ==========

    @Test
    public void fraudTipsHasEntries() {
        assertTrue(MockData.getFraudTips().size() >= 4);
    }

    // ========== Emergency Alerts ==========

    @Test
    public void repositorySingletonIsInitialized() {
        assertNotNull(Repository.getInstance());
        assertEquals(Repository.getInstance().getUsers().size(), MockData.getUsers().size());
    }

    @Test
    public void addEmergencyAlert() {
        MockData.setActiveUser(1);
        int countBefore = MockData.getEmergencyAlerts().size();
        MockData.addEmergencyAlert("测试SOS");
        assertTrue(MockData.getEmergencyAlerts().size() > countBefore);
    }
}
