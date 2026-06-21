package com.silverguardian.prototype;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;

import com.silverguardian.prototype.ai.AiChatModule;
import com.silverguardian.prototype.app.AppContainer;
import com.silverguardian.prototype.community.CommunityPoiSearchModule;
import com.silverguardian.prototype.modules.EmergencyModule;
import com.silverguardian.prototype.modules.FamilyAlbumModule;
import com.silverguardian.prototype.modules.HealthRecordModule;
import com.silverguardian.prototype.modules.MedicineReminderModule;
import com.silverguardian.prototype.modules.MemoryModule;
import com.silverguardian.prototype.modules.SafeCheckModule;
import com.silverguardian.prototype.modules.SafetyContentModule;
import com.silverguardian.prototype.modules.UserSessionModule;
import com.silverguardian.prototype.utils.FontScaleHelper;
import com.silverguardian.prototype.weather.WeatherModule;

public abstract class BaseActivity extends AppCompatActivity {
    protected AppContainer appContainer() { return SilverGuardianApp.from(this); }
    protected UserSessionModule userSession() { return appContainer().userSession(); }
    protected HealthRecordModule healthRecords() { return appContainer().healthRecords(); }
    protected MedicineReminderModule medicineReminders() { return appContainer().medicineReminders(); }
    protected FamilyAlbumModule familyAlbum() { return appContainer().familyAlbum(); }
    protected MemoryModule memories() { return appContainer().memories(); }
    protected SafeCheckModule safeChecks() { return appContainer().safeChecks(); }
    protected EmergencyModule emergencies() { return appContainer().emergencies(); }
    protected SafetyContentModule safetyContent() { return appContainer().safetyContent(); }
    protected CommunityPoiSearchModule communitySearch() { return appContainer().community(); }
    protected AiChatModule aiChat() { return appContainer().aiChat(); }
    protected WeatherModule weather() { return appContainer().weather(); }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(FontScaleHelper.withAppFontScale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getDelegate().setLocalNightMode(FontScaleHelper.isHighContrast(this)
            ? AppCompatDelegate.MODE_NIGHT_YES
            : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
    }
}

