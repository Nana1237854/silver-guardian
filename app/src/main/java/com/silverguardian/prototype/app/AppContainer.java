package com.silverguardian.prototype.app;

import android.content.Context;

import com.silverguardian.prototype.ai.AiChatModule;
import com.silverguardian.prototype.community.CommunityPoiSearchModule;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.modules.EmergencyModule;
import com.silverguardian.prototype.modules.FamilyAlbumModule;
import com.silverguardian.prototype.modules.HealthRecordModule;
import com.silverguardian.prototype.modules.MedicineReminderModule;
import com.silverguardian.prototype.modules.MemoryModule;
import com.silverguardian.prototype.modules.SafetyContentModule;
import com.silverguardian.prototype.modules.UserSessionModule;
import com.silverguardian.prototype.reminder.TtsHelper;

public class AppContainer {
    private final Context appContext;
    private final Repository repository;
    private final UserSessionModule userSessionModule;
    private final HealthRecordModule healthRecordModule;
    private final MedicineReminderModule medicineReminderModule;
    private final FamilyAlbumModule familyAlbumModule;
    private final MemoryModule memoryModule;
    private final EmergencyModule emergencyModule;
    private final SafetyContentModule safetyContentModule;
    private final CommunityPoiSearchModule communityPoiSearchModule;
    private final AiChatModule aiChatModule;

    public AppContainer(Context context) {
        appContext = context.getApplicationContext();
        repository = Repository.init(appContext);
        TtsHelper.init(appContext);
        userSessionModule = new UserSessionModule(repository);
        healthRecordModule = new HealthRecordModule(repository);
        medicineReminderModule = new MedicineReminderModule(repository);
        familyAlbumModule = new FamilyAlbumModule(repository);
        memoryModule = new MemoryModule(repository);
        emergencyModule = new EmergencyModule(repository);
        safetyContentModule = new SafetyContentModule(repository);
        communityPoiSearchModule = new CommunityPoiSearchModule(appContext);
        aiChatModule = new AiChatModule(appContext, repository);
    }

    public Context appContext() { return appContext; }
    public UserSessionModule userSession() { return userSessionModule; }
    public HealthRecordModule healthRecords() { return healthRecordModule; }
    public MedicineReminderModule medicineReminders() { return medicineReminderModule; }
    public FamilyAlbumModule familyAlbum() { return familyAlbumModule; }
    public MemoryModule memories() { return memoryModule; }
    public EmergencyModule emergencies() { return emergencyModule; }
    public SafetyContentModule safetyContent() { return safetyContentModule; }
    public CommunityPoiSearchModule community() { return communityPoiSearchModule; }
    public AiChatModule aiChat() { return aiChatModule; }
}
