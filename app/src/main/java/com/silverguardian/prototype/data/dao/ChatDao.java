package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDao {
    private final ElderlyDbHelper dbHelper;

    public ChatDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<ChatMessage> readAll(int userId) {
        List<ChatMessage> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT content,type,strftime('%H:%M',created_at) FROM messages WHERE user_id=? ORDER BY id", new String[]{String.valueOf(userId)});
        String now = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        while (c.moveToNext()) list.add(new ChatMessage(c.getString(0), c.getString(1), c.getString(2) == null ? now : c.getString(2)));
        c.close();
        return list;
    }

    public void add(int userId, String text, String type) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("content", text); values.put("type", type);
        dbHelper.getWritableDatabase().insert("messages", null, values);
    }

    public void seed(SQLiteDatabase db, long userId) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("content", "你好！我是银发守护助手，有健康问题、用药疑问或生活困扰，随时告诉我。");
        v.put("type", "ai");
        db.insert("messages", null, v);
    }
}
