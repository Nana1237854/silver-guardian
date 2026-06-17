package com.silverguardian.prototype.models;

public class HealthData {
    public String type;
    public String value;
    public String status;
    public int iconResId;

    public HealthData(String type, String value, String status, int iconResId) {
        this.type = type;
        this.value = value;
        this.status = status;
        this.iconResId = iconResId;
    }

    public String getLabel() {
        switch (type) {
            case "heart_rate": return "心率";
            case "steps": return "步数";
            case "sleep": return "睡眠";
            case "exercise": return "运动";
            case "mood": return "心情";
            case "breathing": return "呼吸正念";
            case "blood_pressure": return "血压";
            case "blood_sugar": return "血糖";
            case "blood_oxygen": return "血氧";
            case "temperature": return "体温";
            case "weight": return "体重";
            case "body_fat": return "体脂率";
            case "respiratory_rate": return "呼吸率";
            case "calories": return "热量";
            default: return type;
        }
    }

    public String getUnit() {
        switch (type) {
            case "heart_rate": return "bpm";
            case "steps": return "步";
            case "sleep": return "小时";
            case "exercise": return "分钟";
            case "mood": return "";
            case "breathing": return "分钟";
            case "blood_pressure": return "mmHg";
            case "blood_sugar": return "mmol/L";
            case "blood_oxygen": return "%";
            case "temperature": return "°C";
            case "weight": return "kg";
            case "body_fat": return "%";
            case "respiratory_rate": return "次/分";
            case "calories": return "kcal";
            default: return "";
        }
    }
}
