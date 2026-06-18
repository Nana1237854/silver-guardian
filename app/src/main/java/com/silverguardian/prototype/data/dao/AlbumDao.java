package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.AlbumPhoto;

import java.util.ArrayList;
import java.util.List;

public class AlbumDao {
    private final ElderlyDbHelper dbHelper;

    public AlbumDao(ElderlyDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<AlbumPhoto> readAll(int userId) {
        List<AlbumPhoto> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,url,title,description,category,uploaded_by,favorite,scene_tag,family_message FROM album_photos WHERE user_id=? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) list.add(new AlbumPhoto(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getInt(6) == 1, c.getString(7), c.getString(8)));
        c.close();
        return list;
    }

    public int add(int userId, String title, String category, String message) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId); values.put("url", ""); values.put("title", title);
        values.put("description", "家属上传照片"); values.put("category", category);
        values.put("uploaded_by", "家属"); values.put("favorite", 0);
        values.put("scene_tag", category); values.put("family_message", message);
        return (int) dbHelper.getWritableDatabase().insert("album_photos", null, values);
    }

    public void delete(int photoId) {
        dbHelper.getWritableDatabase().delete("album_photos", "id=?", new String[]{String.valueOf(photoId)});
    }

    public void setFavorite(int photoId, boolean favorite) {
        ContentValues v = new ContentValues();
        v.put("favorite", favorite ? 1 : 0);
        dbHelper.getWritableDatabase().update("album_photos", v, "id=?", new String[]{String.valueOf(photoId)});
    }

    public void seed(SQLiteDatabase db, long userId) {
        ContentValues v1 = new ContentValues();
        v1.put("user_id", userId); v1.put("url", ""); v1.put("title", "春节团聚");
        v1.put("description", "2024 年春节全家福"); v1.put("category", "家庭");
        v1.put("uploaded_by", "小明"); v1.put("favorite", 1); v1.put("scene_tag", "春节团聚");
        v1.put("family_message", "祝爷爷奶奶身体健康！");
        db.insert("album_photos", null, v1);

        ContentValues v2 = new ContentValues();
        v2.put("user_id", userId); v2.put("url", ""); v2.put("title", "公园散步");
        v2.put("description", "春天一起去公园"); v2.put("category", "日常");
        v2.put("uploaded_by", "小柔"); v2.put("favorite", 0); v2.put("scene_tag", "户外活动");
        v2.put("family_message", "天气好要多出去走走");
        db.insert("album_photos", null, v2);
    }
}
