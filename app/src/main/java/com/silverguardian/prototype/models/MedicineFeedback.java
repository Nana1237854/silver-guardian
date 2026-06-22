package com.silverguardian.prototype.models;

// 服药反馈模型：药品ID、反馈内容、时间
public class MedicineFeedback {
    public static final String TYPE_NORMAL = "NORMAL";
    public static final String TYPE_DIZZY = "DIZZY";
    public static final String TYPE_NAUSEA = "NAUSEA";
    public static final String TYPE_PALPITATION = "PALPITATION";
    public static final String TYPE_OTHER = "OTHER";
    public static final String TYPE_SKIPPED = "SKIPPED";

    public final int id;
    public final int userId;
    public final int medicineId;
    public final String medicineName;
    public final String feedbackType;
    public final String feedbackText;
    public final String createdAt;
    public final String date;

    public MedicineFeedback(int id, int userId, int medicineId, String medicineName,
                            String feedbackType, String feedbackText, String createdAt, String date) {
        this.id = id;
        this.userId = userId;
        this.medicineId = medicineId;
        this.medicineName = medicineName != null ? medicineName : "";
        this.feedbackType = feedbackType != null ? feedbackType : TYPE_SKIPPED;
        this.feedbackText = feedbackText != null ? feedbackText : "";
        this.createdAt = createdAt != null ? createdAt : "";
        this.date = date != null ? date : "";
    }

    public static boolean isWarning(String feedbackType) {
        return TYPE_DIZZY.equals(feedbackType)
            || TYPE_NAUSEA.equals(feedbackType)
            || TYPE_PALPITATION.equals(feedbackType)
            || TYPE_OTHER.equals(feedbackType);
    }
}
