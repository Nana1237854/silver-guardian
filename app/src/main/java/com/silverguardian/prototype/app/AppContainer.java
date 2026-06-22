package com.silverguardian.prototype.app;

import android.content.Context;

import com.silverguardian.prototype.ai.AiChatModule;
import com.silverguardian.prototype.community.CommunityPoiSearchModule;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.modules.CareSummaryModule;
import com.silverguardian.prototype.modules.EmergencyModule;
import com.silverguardian.prototype.modules.FamilyAlbumModule;
import com.silverguardian.prototype.modules.HealthRecordModule;
import com.silverguardian.prototype.modules.MedicineFeedbackModule;
import com.silverguardian.prototype.modules.MedicineReminderModule;
import com.silverguardian.prototype.modules.MemoryModule;
import com.silverguardian.prototype.modules.SafeCheckModule;
import com.silverguardian.prototype.modules.SeniorGuideModule;
import com.silverguardian.prototype.modules.SafetyContentModule;
import com.silverguardian.prototype.modules.SosHelpModule;
import com.silverguardian.prototype.modules.UserSessionModule;
import com.silverguardian.prototype.reminder.SafeCheckScheduler;
import com.silverguardian.prototype.reminder.TtsHelper;
import com.silverguardian.prototype.weather.WeatherModule;

// 全局依赖容器：集中创建Repository、Module、Dispatcher、Adapter实例
public class AppContainer {
    private final Context appContext;
    private final Repository repository;
    private final UserSessionModule userSessionModule;
    private final HealthRecordModule healthRecordModule;
    private final MedicineReminderModule medicineReminderModule;
    private final MedicineFeedbackModule medicineFeedbackModule;
    private final FamilyAlbumModule familyAlbumModule;
    private final MemoryModule memoryModule;
    private final SafeCheckModule safeCheckModule;
    private final CareSummaryModule careSummaryModule;
    private final EmergencyModule emergencyModule;
    private final SafetyContentModule safetyContentModule;
    private final SosHelpModule sosHelpModule;
    private final SeniorGuideModule seniorGuideModule;
    private final CommunityPoiSearchModule communityPoiSearchModule;
    private final AiChatModule aiChatModule;
    private final WeatherModule weatherModule;

    public AppContainer(Context context) {
        appContext = context.getApplicationContext();
        repository = Repository.init(appContext);
        TtsHelper.init(appContext);
        userSessionModule = new UserSessionModule(repository);
        healthRecordModule = new HealthRecordModule(repository);
        medicineReminderModule = new MedicineReminderModule(repository);
        medicineFeedbackModule = new MedicineFeedbackModule(repository);
        familyAlbumModule = new FamilyAlbumModule(repository);
        memoryModule = new MemoryModule(repository);
        safeCheckModule = new SafeCheckModule(repository);
        careSummaryModule = new CareSummaryModule(appContext, repository);
        emergencyModule = new EmergencyModule(repository);
        safetyContentModule = new SafetyContentModule(repository);
        sosHelpModule = new SosHelpModule(repository);
        seniorGuideModule = new SeniorGuideModule(appContext);
        communityPoiSearchModule = new CommunityPoiSearchModule(appContext);
        aiChatModule = new AiChatModule(appContext, repository);
        weatherModule = new WeatherModule(appContext);
        SafeCheckScheduler.scheduleForAll(appContext, repository);
    }

    // 各功能模块获取方法
    public Context appContext() { return appContext; }
    public UserSessionModule userSession() { return userSessionModule; }
    public HealthRecordModule healthRecords() { return healthRecordModule; }
    public MedicineReminderModule medicineReminders() { return medicineReminderModule; }
    public MedicineFeedbackModule medicineFeedback() { return medicineFeedbackModule; }
    public FamilyAlbumModule familyAlbum() { return familyAlbumModule; }
    public MemoryModule memories() { return memoryModule; }
    public SafeCheckModule safeChecks() { return safeCheckModule; }
    public CareSummaryModule careSummaries() { return careSummaryModule; }
    public EmergencyModule emergencies() { return emergencyModule; }
    public SafetyContentModule safetyContent() { return safetyContentModule; }
    public SosHelpModule sosHelp() { return sosHelpModule; }
    public SeniorGuideModule seniorGuide() { return seniorGuideModule; }
    public CommunityPoiSearchModule community() { return communityPoiSearchModule; }
    public AiChatModule aiChat() { return aiChatModule; }
    public WeatherModule weather() { return weatherModule; }
}

