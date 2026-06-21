package com.silverguardian.prototype.fragments;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.SilverGuardianApp;
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
import com.silverguardian.prototype.modules.SeniorGuideModule;
import com.silverguardian.prototype.modules.UserSessionModule;
import com.silverguardian.prototype.utils.FontScaleHelper;
import com.silverguardian.prototype.weather.WeatherModule;

public abstract class BaseFragment extends Fragment {
    protected AppContainer appContainer() { return SilverGuardianApp.from(requireContext()); }
    protected UserSessionModule userSession() { return appContainer().userSession(); }
    protected HealthRecordModule healthRecords() { return appContainer().healthRecords(); }
    protected MedicineReminderModule medicineReminders() { return appContainer().medicineReminders(); }
    protected FamilyAlbumModule familyAlbum() { return appContainer().familyAlbum(); }
    protected MemoryModule memories() { return appContainer().memories(); }
    protected SafeCheckModule safeChecks() { return appContainer().safeChecks(); }
    protected EmergencyModule emergencies() { return appContainer().emergencies(); }
    protected SafetyContentModule safetyContent() { return appContainer().safetyContent(); }
    protected SeniorGuideModule seniorGuide() { return appContainer().seniorGuide(); }
    protected CommunityPoiSearchModule communitySearch() { return appContainer().community(); }
    protected AiChatModule aiChat() { return appContainer().aiChat(); }
    protected WeatherModule weather() { return appContainer().weather(); }

    protected int dp(int value) {
        return FontScaleHelper.dp(requireContext(), value);
    }

    protected int sp(int base) {
        return base;
    }

    protected int color(int resId) {
        return getResources().getColor(resId);
    }

    protected void toast(@NonNull String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}

