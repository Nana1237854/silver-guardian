package com.silverguardian.prototype.data.dao;
import android.content.ContentValues;import android.database.Cursor;import com.silverguardian.prototype.R;import com.silverguardian.prototype.data.ElderlyDbHelper;import com.silverguardian.prototype.models.HealthData;import java.util.*;
public class HealthDao{
 private final ElderlyDbHelper h;public HealthDao(ElderlyDbHelper h){this.h=h;}
 public List<HealthData> readAll(int uid){return query(uid,false);}public List<HealthData> readToday(int uid){return query(uid,true);}
 private List<HealthData> query(int uid,boolean today){List<HealthData>r=new ArrayList<>();String w=today?"user_id=? AND date(created_at,'localtime')=date('now','localtime')":"user_id=?";Cursor c=h.getReadableDatabase().rawQuery("SELECT id,type,value,status,COALESCE(notes,''),created_at FROM health_data WHERE "+w+" ORDER BY datetime(created_at) DESC,id DESC",new String[]{String.valueOf(uid)});while(c.moveToNext())r.add(new HealthData(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),R.drawable.ic_info));c.close();return r;}
 public int add(int uid,String type,String value,String status,String notes){ContentValues v=new ContentValues();v.put("user_id",uid);v.put("type",type);v.put("value",value);v.put("status",status);v.put("notes",notes);return(int)h.getWritableDatabase().insertOrThrow("health_data",null,v);}
}