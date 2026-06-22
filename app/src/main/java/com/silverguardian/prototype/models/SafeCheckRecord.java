package com.silverguardian.prototype.models;

// 平安确认记录模型：确认时间、状态、备注
public class SafeCheckRecord {
    public static final String STATUS_OK = "OK";
    public static final String STATUS_UNWELL = "UNWELL";
    public static final String STATUS_NEED_FAMILY = "NEED_FAMILY";
    public static final String STATUS_REMIND_LATER = "REMIND_LATER";
    public static final String STATUS_MISSED = "MISSED";

    public int id;
    public int userId;
    public String status;
    public String note;
    public String checkedAt;
    public String date;

    public SafeCheckRecord(int id, int userId, String status, String note, String checkedAt, String date) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.note = note == null ? "" : note;
        this.checkedAt = checkedAt == null ? "" : checkedAt;
        this.date = date == null ? "" : date;
    }

    public boolean isConfirmed() {
        return STATUS_OK.equals(status)
            || STATUS_UNWELL.equals(status)
            || STATUS_NEED_FAMILY.equals(status);
    }
}
