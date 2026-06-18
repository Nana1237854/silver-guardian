package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.HealthData;

import java.util.ArrayList;
import java.util.List;

public class HealthDao {
    private final ElderlyDbHelper dbHelper;

    public HealthDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<HealthData> readAll(int userId) {
        List<HealthData> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT type,value,COALESCE(notes,'正常') FROM health_data WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) list.add(new HealthData(c.getString(0), c.getString(1), c.getString(2), android.R.drawable.ic_menu_info_details));
        c.close();
        return list;
    }

    public void add(int userId, String type, String value, String status) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("type", type);
        values.put("value", value);
        values.put("notes", status);
        dbHelper.getWritableDatabase().insert("health_data", null, values);
    }

    public void seed(SQLiteDatabase db, long userId) {
        addSeed(db, userId, "sleep", "5小时14分钟", "注意");
        addSeed(db, userId, "exercise", "206千卡", "良好");
        addSeed(db, userId, "mood", "良好", "今天 20:10 记录");
        addSeed(db, userId, "steps", "2265/10000", "22%");
        addSeed(db, userId, "breathing", "0/3", "分钟");
        addSeed(db, userId, "heart_rate", "48", "偏低");
        addSeed(db, userId, "blood_pressure", "128/82", "正常");
    }

    private void addSeed(SQLiteDatabase db, long userId, String type, String value, String notes) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("type", type); v.put("value", value); v.put("notes", notes);
        db.insert("health_data", null, v);
    }
}
