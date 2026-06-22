package com.silverguardian.prototype.models;
// 健康记录数据模型：指标类型、数值、单位、记录时间
public class HealthData {
 public int id,iconResId; public String type,value,status,notes,createdAt;
 public HealthData(String type,String value,String status,int iconResId){this(0,type,value,status,"","",iconResId);}
 public HealthData(int id,String type,String value,String status,String notes,String createdAt,int iconResId){this.id=id;this.type=type;this.value=value;this.status=status;this.notes=notes==null?"":notes;this.createdAt=createdAt==null?"":createdAt;this.iconResId=iconResId;}
 public String getLabel(){switch(type){case "heart_rate":return "心率";case "steps":return "步数";case "sleep":return "睡眠";case "exercise":return "运动";case "mood":return "心情";case "breathing":return "呼吸正念";case "blood_pressure":return "血压";case "blood_sugar":return "血糖";case "blood_oxygen":return "血氧";case "temperature":return "体温";case "weight":return "体重";case "body_fat":return "体脂率";case "respiratory_rate":return "呼吸率";default:return type;}}
 public String getUnit(){switch(type){case "heart_rate":return "bpm";case "steps":return "步";case "sleep":return "小时";case "exercise":case "breathing":return "分钟";case "blood_pressure":return "mmHg";case "blood_sugar":return "mmol/L";case "blood_oxygen":case "body_fat":return "%";case "temperature":return "°C";case "weight":return "kg";case "respiratory_rate":return "次/分";default:return "";}}
}