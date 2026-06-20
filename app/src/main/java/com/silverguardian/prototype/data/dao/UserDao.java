package com.silverguardian.prototype.data.dao;
import android.content.ContentValues;import android.database.Cursor;import android.database.sqlite.SQLiteDatabase;import com.silverguardian.prototype.data.ElderlyDbHelper;import com.silverguardian.prototype.models.User;import java.util.*;
public class UserDao{
 private final ElderlyDbHelper h;public UserDao(ElderlyDbHelper h){this.h=h;}
 public List<User> readAll(){List<User> r=new ArrayList<>();Cursor c=h.getReadableDatabase().rawQuery("SELECT id,name,pin,avatar,age,health_conditions,hint_question,hint_answer FROM users ORDER BY id",null);while(c.moveToNext())r.add(new User(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4),c.getString(5),c.getString(6),c.getString(7)));c.close();return r;}
 public int add(String n,String p,int a,String cond,String q,String ans){return add(h.getWritableDatabase(),n,p,a,cond,q,ans);}
 public int add(SQLiteDatabase db,String n,String p,int a,String cond,String q,String ans){ContentValues v=new ContentValues();v.put("name",n);v.put("pin",p);v.put("avatar","");v.put("age",a);v.put("health_conditions",cond);v.put("hint_question",q);v.put("hint_answer",ans);return(int)db.insertOrThrow("users",null,v);}
 public void delete(int id){h.getWritableDatabase().delete("users","id=?",new String[]{String.valueOf(id)});}
 public void seedIfEmpty(SQLiteDatabase db){Cursor c=db.rawQuery("SELECT COUNT(*) FROM users",null);boolean empty=c.moveToFirst()&&c.getInt(0)==0;c.close();if(!empty)return;add(db,"颜爷爷","1234",72,"高血压、冠心病","","");add(db,"林奶奶","5678",68,"糖尿病、骨质疏松","","");}
}