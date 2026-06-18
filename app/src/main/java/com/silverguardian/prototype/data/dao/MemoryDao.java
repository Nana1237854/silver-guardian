package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.MemoryRecord;

import java.util.ArrayList;
import java.util.List;

public class MemoryDao {
    private final ElderlyDbHelper dbHelper;

    public MemoryDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<MemoryRecord> readAll(int userId) {
        List<MemoryRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,memory,category FROM memories WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) list.add(new MemoryRecord(c.getInt(0), c.getString(1), c.getString(2), "最近"));
        c.close();
        return list;
    }

    public int add(int userId, String content, String category) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("memory", content); values.put("category", category);
        return (int) dbHelper.getWritableDatabase().insert("memories", null, values);
    }

    public void delete(int memoryId) {
        dbHelper.getWritableDatabase().delete("memories", "id=?", new String[]{String.valueOf(memoryId)});
    }

    public void seed(SQLiteDatabase db, long userId) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("memory", "喜欢每天傍晚去小区花园散步。"); v.put("category", "爱好");
        db.insert("memories", null, v);
    }
}
