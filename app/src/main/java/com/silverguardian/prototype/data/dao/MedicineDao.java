package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.Medicine;

import java.util.ArrayList;
import java.util.List;

public class MedicineDao {
    private final ElderlyDbHelper dbHelper;

    public MedicineDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Medicine> readAll(int userId, String today) {
        List<Medicine> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT m.id,m.name,m.type,m.time,m.method,m.description,m.image,CASE WHEN t.id IS NULL THEN 0 ELSE 1 END FROM user_medicines m LEFT JOIN medicine_taken t ON t.medicine_id=m.id AND t.taken_date=? WHERE m.user_id=? ORDER BY m.id", new String[]{today, String.valueOf(userId)});
        while (c.moveToNext()) list.add(new Medicine(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getInt(7) == 1));
        c.close();
        return list;
    }

    public int add(int userId, String name, String type, String time, String method, String description) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("name", name); values.put("type", type);
        values.put("time", time); values.put("method", method); values.put("description", description);
        values.put("image", "");
        return (int) dbHelper.getWritableDatabase().insert("user_medicines", null, values);
    }

    public void delete(int medicineId) {
        dbHelper.getWritableDatabase().delete("user_medicines", "id=?", new String[]{String.valueOf(medicineId)});
    }

    public void setTaken(int userId, int medicineId, boolean taken, String today) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (taken) {
            ContentValues v = new ContentValues();
            v.put("user_id", userId); v.put("medicine_id", medicineId); v.put("taken_date", today);
            db.insert("medicine_taken", null, v);
        } else {
            db.delete("medicine_taken", "user_id=? AND medicine_id=? AND taken_date=?", new String[]{String.valueOf(userId), String.valueOf(medicineId), today});
        }
    }

    public void seed(SQLiteDatabase db, long userId, String today) {
        addSeed(db, userId, "硝苯地平缓释片", "降压药", "08:00,20:00", "口服", "餐后服用，每次 1 片", true, today);
        addSeed(db, userId, "阿托伐他汀钙片 10mg", "降血脂药", "20:00", "口服", "睡前服用", false, today);
        addSeed(db, userId, "维生素D3胶囊 1000IU", "维生素", "12:00", "口服", "随餐服用", true, today);
        addSeed(db, userId, "碳酸钙D3片", "钙片", "16:00", "口服", "随餐服用", false, today);
    }

    private void addSeed(SQLiteDatabase db, long userId, String name, String type, String time, String method, String desc, boolean taken, String today) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("name", name); v.put("type", type); v.put("time", time);
        v.put("method", method); v.put("description", desc); v.put("image", "");
        long medId = db.insert("user_medicines", null, v);
        if (taken) {
            ContentValues vt = new ContentValues();
            vt.put("user_id", userId); vt.put("medicine_id", medId); vt.put("taken_date", today);
            db.insert("medicine_taken", null, vt);
        }
    }
}
