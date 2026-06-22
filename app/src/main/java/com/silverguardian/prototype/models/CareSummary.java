package com.silverguardian.prototype.models;

// 照护摘要数据模型：汇总健康/用药/SOS/平安确认多维度
public class CareSummary {
    public final String safeCheckStatus;
    public final int medicineTotal;
    public final int medicineTaken;
    public final int abnormalHealthCount;
    public final int medicineFeedbackWarningCount;
    public final int emergencyAlertCount;
    public final int newPhotoCount;
    public final boolean fraudTrainingDone;
    public final String summaryText;

    public CareSummary(String safeCheckStatus, int medicineTotal, int medicineTaken,
                       int abnormalHealthCount, int medicineFeedbackWarningCount,
                       int emergencyAlertCount, int newPhotoCount,
                       boolean fraudTrainingDone, String summaryText) {
        this.safeCheckStatus = safeCheckStatus == null ? "" : safeCheckStatus;
        this.medicineTotal = medicineTotal;
        this.medicineTaken = medicineTaken;
        this.abnormalHealthCount = abnormalHealthCount;
        this.medicineFeedbackWarningCount = medicineFeedbackWarningCount;
        this.emergencyAlertCount = emergencyAlertCount;
        this.newPhotoCount = newPhotoCount;
        this.fraudTrainingDone = fraudTrainingDone;
        this.summaryText = summaryText == null ? "" : summaryText;
    }
}
