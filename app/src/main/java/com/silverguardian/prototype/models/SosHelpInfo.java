package com.silverguardian.prototype.models;

/**
 * 一键求助结构化信息。
 * 由 SosHelpModule 聚合现有模块数据后生成，供 UI 层展示、复制或拨号使用。
 */
public class SosHelpInfo {
    public final String elderName;
    public final String locationText;
    public final String healthStatusText;
    public final String medicineStatusText;
    public final String familyContactName;
    public final String familyContactPhone;
    public final String alertStatusText;
    public final String fullMessage;

    public SosHelpInfo(String elderName, String locationText, String healthStatusText,
                       String medicineStatusText, String familyContactName,
                       String familyContactPhone, String alertStatusText, String fullMessage) {
        this.elderName = elderName != null ? elderName : "";
        this.locationText = locationText != null ? locationText : "";
        this.healthStatusText = healthStatusText != null ? healthStatusText : "";
        this.medicineStatusText = medicineStatusText != null ? medicineStatusText : "";
        this.familyContactName = familyContactName != null ? familyContactName : "";
        this.familyContactPhone = familyContactPhone != null ? familyContactPhone : "";
        this.alertStatusText = alertStatusText != null ? alertStatusText : "";
        this.fullMessage = fullMessage != null ? fullMessage : "";
    }
}
