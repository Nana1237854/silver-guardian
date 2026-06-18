package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.EmergencyAlert;

import java.util.ArrayList;
import java.util.List;

public class EmergencyDao {
    private final ElderlyDbHelper dbHelper;

    public EmergencyDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<EmergencyAlert> readAll(int userId) {
        List<EmergencyAlert> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,created_at,message FROM emergency_alerts WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) list.add(new EmergencyAlert(c.getInt(0), "今天", c.getString(2), "已响应"));
        c.close();
        return list;
    }

    public int add(int userId, String message) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("keyword", "SOS"); values.put("message", message);
        return (int) dbHelper.getWritableDatabase().insert("emergency_alerts", null, values);
    }

    public void seed(SQLiteDatabase db, long userId) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("keyword", "SOS");
        v.put("message", "今天 09:12 模拟 SOS 求助已发送给大明、小柔。");
        db.insert("emergency_alerts", null, v);
    }
}
