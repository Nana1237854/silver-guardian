package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.FamilyMember;

import java.util.ArrayList;
import java.util.List;

// 家属联系人数据访问：家属信息增删改查
public class FamilyDao {
    private final ElderlyDbHelper dbHelper;

    public FamilyDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<FamilyMember> readAll(int userId) {
        List<FamilyMember> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,name,relationship,phone,avatar,status FROM family_members WHERE user_id=? ORDER BY id", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) list.add(new FamilyMember(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), "online".equals(c.getString(5))));
        c.close();
        return list;
    }

    public long add(SQLiteDatabase db, int userId, String name, String relationship, String phone) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        values.put("relationship", relationship);
        values.put("phone", phone);
        values.put("avatar", "");
        values.put("status", "offline");
        return db.insertOrThrow("family_members", null, values);
    }

    // 更新家属联系人信息
    public int update(SQLiteDatabase db, int id, String name, String relationship, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("relationship", relationship);
        values.put("phone", phone);
        return db.update("family_members", values, "id=?", new String[]{String.valueOf(id)});
    }

    // 删除家属联系人
    public void delete(SQLiteDatabase db, int id) {
        db.delete("family_members", "id=?", new String[]{String.valueOf(id)});
    }

    public void seed(SQLiteDatabase db, long userId) {
        ContentValues v1 = new ContentValues();
        v1.put("user_id", userId); v1.put("name", "大明"); v1.put("relationship", "儿子");
        v1.put("phone", "13800001111"); v1.put("avatar", ""); v1.put("status", "online");
        db.insert("family_members", null, v1);

        ContentValues v2 = new ContentValues();
        v2.put("user_id", userId); v2.put("name", "小柔"); v2.put("relationship", "女儿");
        v2.put("phone", "13800002222"); v2.put("avatar", ""); v2.put("status", "online");
        db.insert("family_members", null, v2);
    }
}
