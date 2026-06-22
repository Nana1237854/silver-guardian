package com.silverguardian.prototype.modules;

import android.content.Context;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.health.HealthMetricEvaluator;
import com.silverguardian.prototype.models.CareSummary;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.SafeCheckRecord;

import java.util.List;

// 照护摘要汇总模块：聚合健康、用药、SOS、平安确认等多维度数据
public class CareSummaryModule {
    private final Context appContext;
    private final Repository repository;

    public CareSummaryModule(Context appContext, Repository repository) {
        this.appContext = appContext.getApplicationContext();
        this.repository = repository;
    }

    public CareSummary getTodaySummary() {
        String safeCheckStatus = "";
        SafeCheckRecord safeCheckRecord = repository.getTodaySafeCheckRecord();
        if (safeCheckRecord != null) {
            safeCheckStatus = safeCheckRecord.status;
        }

        int medicineTotal = repository.getMedicines().size();
        int medicineTaken = 0;
        for (int i = 0; i < repository.getMedicines().size(); i++) {
            if (repository.getMedicines().get(i).takenToday) {
                medicineTaken++;
            }
        }

        int abnormalHealthCount = 0;
        List<HealthData> healthData = repository.getTodayHealthData();
        for (int i = 0; i < healthData.size(); i++) {
            if (HealthMetricEvaluator.isAbnormal(healthData.get(i))) {
                abnormalHealthCount++;
            }
        }

        int emergencyAlertCount = repository.getTodayEmergencyAlertCount();
        int newPhotoCount = repository.getTodayPhotoCount();
        int medicineFeedbackWarningCount = repository.getTodayMedicineFeedbackWarningCount();
        boolean fraudTrainingDone = false;

        String summaryText = appContext.getString(
            R.string.care_summary_overview_value,
            getSafeCheckStatusText(safeCheckStatus),
            getMedicineText(medicineTotal, medicineTaken),
            getHealthText(abnormalHealthCount, healthData.isEmpty()),
            getMedicineFeedbackText(medicineFeedbackWarningCount),
            getAlertText(emergencyAlertCount),
            getFamilyText(newPhotoCount),
            getFraudText(fraudTrainingDone)
        );

        return new CareSummary(
            safeCheckStatus,
            medicineTotal,
            medicineTaken,
            abnormalHealthCount,
            medicineFeedbackWarningCount,
            emergencyAlertCount,
            newPhotoCount,
            fraudTrainingDone,
            summaryText
        );
    }

    public String getSafeCheckStatusText(String status) {
        if (SafeCheckRecord.STATUS_OK.equals(status)) {
            return appContext.getString(R.string.care_summary_safe_check_ok);
        }
        if (SafeCheckRecord.STATUS_UNWELL.equals(status)) {
            return appContext.getString(R.string.care_summary_safe_check_unwell);
        }
        if (SafeCheckRecord.STATUS_NEED_FAMILY.equals(status)) {
            return appContext.getString(R.string.care_summary_safe_check_need_family);
        }
        if (SafeCheckRecord.STATUS_MISSED.equals(status)) {
            return appContext.getString(R.string.care_summary_safe_check_missed);
        }
        if (SafeCheckRecord.STATUS_REMIND_LATER.equals(status)) {
            return appContext.getString(R.string.care_summary_safe_check_remind_later);
        }
        return appContext.getString(R.string.care_summary_safe_check_pending);
    }

    public String getMedicineText(int total, int taken) {
        if (total <= 0) {
            return appContext.getString(R.string.care_summary_medicine_empty);
        }
        return appContext.getString(R.string.care_summary_medicine_value, taken, total);
    }

    public String getHealthText(int abnormalCount, boolean empty) {
        if (abnormalCount > 0) {
            return appContext.getString(R.string.care_summary_health_warning_value, abnormalCount);
        }
        if (empty) {
            return appContext.getString(R.string.care_summary_health_empty);
        }
        return appContext.getString(R.string.care_summary_health_normal);
    }

    public String getMedicineFeedbackText(int warningCount) {
        if (warningCount > 0) {
            return appContext.getString(R.string.care_summary_feedback_warning_value, warningCount);
        }
        if (!repository.getTodayMedicineFeedbacks().isEmpty()) {
            return appContext.getString(R.string.care_summary_feedback_recorded);
        }
        return appContext.getString(R.string.care_summary_feedback_empty);
    }

    public String getAlertText(int alertCount) {
        if (alertCount > 0) {
            return appContext.getString(R.string.care_summary_alert_warning_value, alertCount);
        }
        return appContext.getString(R.string.care_summary_alert_empty);
    }

    public String getFamilyText(int photoCount) {
        if (photoCount > 0) {
            return appContext.getString(R.string.care_summary_family_value, photoCount);
        }
        return appContext.getString(R.string.care_summary_family_empty);
    }

    public String getFraudText(boolean done) {
        if (done) {
            return appContext.getString(R.string.care_summary_fraud_done);
        }
        return appContext.getString(R.string.care_summary_fraud_empty);
    }

    public boolean hasPriorityWarning(CareSummary summary) {
        return summary.emergencyAlertCount > 0
            || SafeCheckRecord.STATUS_MISSED.equals(summary.safeCheckStatus);
    }
}
