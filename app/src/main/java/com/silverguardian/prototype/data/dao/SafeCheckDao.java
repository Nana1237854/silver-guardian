package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.SafeCheckRecord;

// 平安确认记录数据读写
public class SafeCheckDao {
    private final ElderlyDbHelper dbHelper;

    public SafeCheckDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public SafeCheckRecord readToday(int userId, String date) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT id,user_id,status,COALESCE(note,''),COALESCE(checked_at,''),record_date " +
                "FROM safe_check_records WHERE user_id=? AND record_date=? LIMIT 1",
            new String[]{String.valueOf(userId), date});
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new SafeCheckRecord(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5));
        } finally {
            cursor.close();
        }
    }

    public SafeCheckRecord upsertToday(int userId, String status, String note, String checkedAt, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SafeCheckRecord existing = readToday(userId, date);
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("status", status);
        values.put("note", note);
        values.put("checked_at", checkedAt);
        values.put("record_date", date);
        if (existing == null) {
            long id = db.insertOrThrow("safe_check_records", null, values);
            return new SafeCheckRecord((int) id, userId, status, note, checkedAt, date);
        }
        db.update("safe_check_records", values, "id=?", new String[]{String.valueOf(existing.id)});
        return new SafeCheckRecord(existing.id, userId, status, note, checkedAt, date);
    }
}
