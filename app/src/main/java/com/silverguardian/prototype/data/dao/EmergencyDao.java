package com.silverguardian.prototype.data.dao;
import android.content.ContentValues;import android.database.Cursor;import com.silverguardian.prototype.data.ElderlyDbHelper;import com.silverguardian.prototype.models.EmergencyAlert;import java.util.*;
// 紧急提醒记录读写：SOS求助事件持久化
public class EmergencyDao{
 private final ElderlyDbHelper h;public EmergencyDao(ElderlyDbHelper h){this.h=h;}
 public List<EmergencyAlert> readAll(int uid){List<EmergencyAlert>r=new ArrayList<>();Cursor c=h.getReadableDatabase().rawQuery("SELECT id,created_at,message,status FROM emergency_alerts WHERE user_id=? ORDER BY id DESC",new String[]{String.valueOf(uid)});while(c.moveToNext())r.add(new EmergencyAlert(c.getInt(0),c.getString(1),c.getString(2),c.getString(3)));c.close();return r;}
 public int add(int uid,String message){ContentValues v=new ContentValues();v.put("user_id",uid);v.put("keyword","SOS");v.put("message",message);v.put("status","待处理");return(int)h.getWritableDatabase().insertOrThrow("emergency_alerts",null,v);}
}