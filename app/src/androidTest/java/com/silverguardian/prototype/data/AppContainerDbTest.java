package com.silverguardian.prototype.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.silverguardian.prototype.app.AppContainer;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.models.MemoryRecord;
import com.silverguardian.prototype.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AppContainerDbTest {

    private AppContainer container;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("elderly_guardian.db");
        container = new AppContainer(context);
    }

    @Test
    public void initCreatesDefaultUsers() {
        List<User> users = container.userSession().getUsers();
        assertTrue("Should have seeded users", users.size() >= 2);
        assertNotNull(users.get(0).name);
        assertFalse(users.get(0).pin.isEmpty());
    }

    @Test
    public void addAndDeleteUserThroughUserSession() {
        int before = container.userSession().getUsers().size();
        User added = container.userSession().addUser("Test Elder", "9999", 70, "Test");
        assertNotNull(added);
        assertEquals(before + 1, container.userSession().getUsers().size());

        container.userSession().deleteUser(added);
        assertEquals(before, container.userSession().getUsers().size());
    }

    @Test
    public void switchingActiveUserChangesModuleContext() {
        container.userSession().setActiveUser(1);
        int userOneCount = container.medicineReminders().getMedicines().size();

        container.userSession().setActiveUser(2);
        int userTwoBefore = container.medicineReminders().getMedicines().size();
        container.medicineReminders().addMedicine("User2 Med", "TestType", "08:00", "Oral", "Only for user two");
        int userTwoAfter = container.medicineReminders().getMedicines().size();
        assertEquals(userTwoBefore + 1, userTwoAfter);

        container.userSession().setActiveUser(1);
        List<Medicine> userOneMedicines = container.medicineReminders().getMedicines();
        assertEquals(userOneCount, userOneMedicines.size());
        for (Medicine medicine : userOneMedicines) {
            assertFalse("Medicine added for user 2 should not appear for user 1", "User2 Med".equals(medicine.name));
        }
    }

    @Test
    public void medicineCrudWorksThroughModule() {
        container.userSession().setActiveUser(1);
        Medicine medicine = container.medicineReminders().addMedicine("Test Med", "Tablet", "08:00", "Oral", "Daily");
        assertNotNull(medicine);

        List<Medicine> medicines = container.medicineReminders().getMedicines();
        assertTrue(medicines.size() > 0);
        assertEquals("Test Med", medicines.get(0).name);

        container.medicineReminders().toggleMedicineTaken(medicine, true);
        assertTrue(medicine.takenToday);

        int beforeDelete = container.medicineReminders().getMedicines().size();
        container.medicineReminders().deleteMedicine(medicine);
        assertEquals(beforeDelete - 1, container.medicineReminders().getMedicines().size());
    }

    @Test
    public void albumCrudWorksThroughModule() {
        container.userSession().setActiveUser(1);
        AlbumPhoto photo = container.familyAlbum().addPhoto("Test Photo", "family", "Hello");
        assertNotNull(photo);
        assertEquals("Test Photo", photo.title);

        boolean wasFavorite = photo.favorite;
        container.familyAlbum().toggleFavorite(photo);
        assertEquals(!wasFavorite, photo.favorite);

        int beforeDelete = container.familyAlbum().getPhotos().size();
        container.familyAlbum().deletePhoto(photo);
        assertEquals(beforeDelete - 1, container.familyAlbum().getPhotos().size());
    }

    @Test
    public void addChatMessagePersistsInRepository() {
        container.userSession().setActiveUser(1);
        int before = Repository.getInstance().getWelcomeMessages().size();
        Repository.getInstance().addChatMessage("Hello from test", "user");
        assertEquals(before + 1, Repository.getInstance().getWelcomeMessages().size());
    }

    @Test
    public void addHealthDataAppearsThroughModule() {
        container.userSession().setActiveUser(1);
        int before = container.healthRecords().getHealthData().size();
        HealthData added = container.healthRecords().addHealthData("heart_rate", "72", "normal");
        assertNotNull(added);
        assertTrue(container.healthRecords().getHealthData().size() > before);
    }

    @Test
    public void memoryCrudWorksThroughModule() {
        container.userSession().setActiveUser(1);
        int before = container.memories().getMemories().size();
        MemoryRecord record = container.memories().addMemory("Remember the park walk", "daily");
        assertNotNull(record);
        assertEquals(before + 1, container.memories().getMemories().size());

        container.memories().deleteMemory(record);
        assertEquals(before, container.memories().getMemories().size());
    }

    @Test
    public void medicineLibraryAndFraudTipsRemainAvailable() {
        List<MedicineLibraryItem> library = container.medicineReminders().searchMedicineLibrary("高血压");
        assertTrue(container.medicineReminders().getMedicineTypes().size() >= 1);
        assertTrue(library.size() >= 1);
        assertTrue(container.safetyContent().getFraudTips().size() >= 4);
    }

    @Test
    public void emergencyAlertsWorkThroughModule() {
        container.userSession().setActiveUser(1);
        int before = container.emergencies().getAlerts().size();
        EmergencyAlert alert = container.emergencies().addAlert("Test SOS");
        assertNotNull(alert);
        assertTrue(container.emergencies().getAlerts().size() > before);
    }

    @Test
    public void repositorySingletonIsInitializedByContainer() {
        assertNotNull(Repository.getInstance());
        assertEquals(Repository.getInstance().getUsers().size(), container.userSession().getUsers().size());
    }
}