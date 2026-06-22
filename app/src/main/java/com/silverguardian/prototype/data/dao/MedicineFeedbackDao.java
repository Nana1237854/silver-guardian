package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.MedicineFeedback;

import java.util.ArrayList;
import java.util.List;

// 服药反馈数据读写
public class MedicineFeedbackDao {
    private final ElderlyDbHelper dbHelper;

    public MedicineFeedbackDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long add(SQLiteDatabase db, int userId, int medicineId, String medicineName,
                    String feedbackType, String feedbackText, String date, String createdAt) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("medicine_id", medicineId);
        values.put("medicine_name", medicineName != null ? medicineName : "");
        values.put("feedback_type", feedbackType != null ? feedbackType : MedicineFeedback.TYPE_SKIPPED);
        values.put("feedback_text", feedbackText != null ? feedbackText : "");
        values.put("date", date);
        values.put("created_at", createdAt);
        return db.insertOrThrow("medicine_feedback", null, values);
    }

    public List<MedicineFeedback> readByDate(int userId, String date) {
        List<MedicineFeedback> result = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT id, user_id, medicine_id, COALESCE(medicine_name,''), feedback_type, " +
                "COALESCE(feedback_text,''), COALESCE(created_at,''), date " +
                "FROM medicine_feedback WHERE user_id=? AND date=? ORDER BY id DESC",
            new String[]{String.valueOf(userId), date});
        while (cursor.moveToNext()) {
            result.add(new MedicineFeedback(
                cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7)));
        }
        cursor.close();
        return result;
    }

    public List<MedicineFeedback> readAll(int userId) {
        List<MedicineFeedback> result = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT id, user_id, medicine_id, COALESCE(medicine_name,''), feedback_type, " +
                "COALESCE(feedback_text,''), COALESCE(created_at,''), date " +
                "FROM medicine_feedback WHERE user_id=? ORDER BY id DESC",
            new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            result.add(new MedicineFeedback(
                cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7)));
        }
        cursor.close();
        return result;
    }

    public int countWarningsByDate(int userId, String date) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM medicine_feedback WHERE user_id=? AND date=? AND " +
                "feedback_type IN (?,?,?,?)",
            new String[]{String.valueOf(userId), date,
                MedicineFeedback.TYPE_DIZZY, MedicineFeedback.TYPE_NAUSEA,
                MedicineFeedback.TYPE_PALPITATION, MedicineFeedback.TYPE_OTHER});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<MedicineFeedback> readWarningsByDate(int userId, String date) {
        List<MedicineFeedback> result = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT id, user_id, medicine_id, COALESCE(medicine_name,''), feedback_type, " +
                "COALESCE(feedback_text,''), COALESCE(created_at,''), date " +
                "FROM medicine_feedback WHERE user_id=? AND date=? AND " +
                "feedback_type IN (?,?,?,?) ORDER BY id DESC",
            new String[]{String.valueOf(userId), date,
                MedicineFeedback.TYPE_DIZZY, MedicineFeedback.TYPE_NAUSEA,
                MedicineFeedback.TYPE_PALPITATION, MedicineFeedback.TYPE_OTHER});
        while (cursor.moveToNext()) {
            result.add(new MedicineFeedback(
                cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7)));
        }
        cursor.close();
        return result;
    }
}
