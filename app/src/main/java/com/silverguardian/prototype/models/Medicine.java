package com.silverguardian.prototype.models;
// 用户药品数据模型：药品名、剂量、时间、打卡状态
public class Medicine {
 public int id; public String name,type,time,method,description,imagePath; public boolean takenToday; public int advanceMinutes,repeatCount,repeatInterval;
 public Medicine(int id,String name,String type,String time,String method,String description,String imagePath,boolean takenToday){this(id,name,type,time,method,description,imagePath,takenToday,0,0,10);}
 public Medicine(int id,String name,String type,String time,String method,String description,String imagePath,boolean takenToday,int advanceMinutes,int repeatCount,int repeatInterval){this.id=id;this.name=name;this.type=type;this.time=time;this.method=method;this.description=description;this.imagePath=imagePath;this.takenToday=takenToday;this.advanceMinutes=advanceMinutes;this.repeatCount=repeatCount;this.repeatInterval=repeatInterval;}
}