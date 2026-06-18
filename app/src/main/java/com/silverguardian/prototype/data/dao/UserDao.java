package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final ElderlyDbHelper dbHelper;

    public UserDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<User> readAll() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,name,pin,avatar,age,health_conditions FROM users ORDER BY id", null);
        while (c.moveToNext()) users.add(new User(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5)));
        c.close();
        return users;
    }

    public int add(String name, String pin, int age, String conditions) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("pin", pin);
        values.put("avatar", "");
        values.put("age", age);
        values.put("health_conditions", conditions);
        return (int) dbHelper.getWritableDatabase().insert("users", null, values);
    }

    public void delete(int userId) {
        dbHelper.getWritableDatabase().delete("users", "id=?", new String[]{String.valueOf(userId)});
    }

    public void seedIfEmpty(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM users", null);
        boolean empty = true;
        if (c.moveToFirst()) empty = c.getInt(0) == 0;
        c.close();
        if (!empty) return;

        ContentValues v1 = new ContentValues();
        v1.put("name", "颜爷爷"); v1.put("pin", "1234"); v1.put("avatar", "");
        v1.put("age", 72); v1.put("health_conditions", "高血压、冠心病");
        db.insert("users", null, v1);

        ContentValues v2 = new ContentValues();
        v2.put("name", "林奶奶"); v2.put("pin", "5678"); v2.put("avatar", "");
        v2.put("age", 68); v2.put("health_conditions", "糖尿病、骨质疏松");
        db.insert("users", null, v2);
    }
}
