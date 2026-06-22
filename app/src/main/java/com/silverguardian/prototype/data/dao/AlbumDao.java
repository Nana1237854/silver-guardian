package com.silverguardian.prototype.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.silverguardian.prototype.data.ElderlyDbHelper;
import com.silverguardian.prototype.models.Album;
import com.silverguardian.prototype.models.AlbumPhoto;

import java.util.ArrayList;
import java.util.List;

// 相册和照片数据访问：相册/照片CRUD
public class AlbumDao {
    private final ElderlyDbHelper h;

    public AlbumDao(ElderlyDbHelper h) {
        this.h = h;
    }

    // 查询用户所有照片：按ID倒序返回
    public List<AlbumPhoto> readAll(int uid) {
        List<AlbumPhoto> result = new ArrayList<>();
        Cursor c = h.getReadableDatabase().rawQuery(
            "SELECT id,COALESCE(album_id,0),url,COALESCE(public_uri,''),title,description,category,uploaded_by,favorite,scene_tag,family_message FROM album_photos WHERE user_id=? ORDER BY id DESC",
            new String[]{String.valueOf(uid)});
        while (c.moveToNext()) {
            result.add(new AlbumPhoto(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4),
                c.getString(5), c.getString(6), c.getString(7), c.getInt(8) == 1, c.getString(9), c.getString(10)));
        }
        c.close();
        return result;
    }

    // 创建新相册：按用户ID和相册名插入，返回相册ID
    public int createAlbum(int userId, String name) {
        SQLiteDatabase db = h.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        db.insertWithOnConflict("albums", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Cursor c = db.rawQuery("SELECT id FROM albums WHERE user_id=? AND name=? LIMIT 1",
            new String[]{String.valueOf(userId), name});
        int albumId = 0;
        if (c.moveToFirst()) {
            albumId = c.getInt(0);
        }
        c.close();
        return albumId;
    }

    // 查询用户所有相册：含封面URL、照片数量统计
    public List<Album> readAlbums(int userId) {
        List<Album> albums = new ArrayList<>();
        Cursor c = h.getReadableDatabase().rawQuery(
            "SELECT a.id,a.name,COALESCE(NULLIF(a.cover_url,''),(SELECT p.url FROM album_photos p WHERE p.album_id=a.id ORDER BY p.id DESC LIMIT 1),'')," +
                "(SELECT COUNT(*) FROM album_photos p WHERE p.album_id=a.id OR (p.album_id IS NULL AND COALESCE(p.category,'Other')=a.name AND p.user_id=a.user_id)),a.created_at " +
                "FROM albums a WHERE a.user_id=? ORDER BY a.id DESC",
            new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            albums.add(new Album(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4)));
        }
        c.close();
        return albums;
    }

    public void deleteAlbum(int albumId) {
        SQLiteDatabase db = h.getWritableDatabase();
        db.delete("album_photos", "album_id=?", new String[]{String.valueOf(albumId)});
        db.delete("albums", "id=?", new String[]{String.valueOf(albumId)});
    }

    public List<AlbumPhoto> readPhotosForAlbum(int userId, int albumId, String albumName) {
        List<AlbumPhoto> result = new ArrayList<>();
        Cursor c = h.getReadableDatabase().rawQuery(
            "SELECT id,COALESCE(album_id,0),url,COALESCE(public_uri,''),title,description,category,uploaded_by,favorite,scene_tag,family_message " +
                "FROM album_photos WHERE user_id=? AND (album_id=? OR (album_id IS NULL AND COALESCE(category,'Other')=?)) ORDER BY id DESC",
            new String[]{String.valueOf(userId), String.valueOf(albumId), albumName});
        while (c.moveToNext()) {
            result.add(new AlbumPhoto(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4),
                c.getString(5), c.getString(6), c.getString(7), c.getInt(8) == 1, c.getString(9), c.getString(10)));
        }
        c.close();
        return result;
    }

    // 插入照片到指定相册：存储私有路径、公共URI、标题等信息
    public int addPhoto(int userId, int albumId, String privatePath, String publicUri, String title, String category, String message) {
        SQLiteDatabase db = h.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("album_id", albumId);
        values.put("url", privatePath);
        values.put("public_uri", publicUri);
        values.put("title", title);
        values.put("description", "Family uploaded photo");
        values.put("category", category);
        values.put("uploaded_by", "Family");
        values.put("scene_tag", category);
        values.put("family_message", message);
        int photoId = (int) db.insertOrThrow("album_photos", null, values);

        ContentValues albumValues = new ContentValues();
        albumValues.put("cover_url", privatePath);
        db.update("albums", albumValues, "id=?", new String[]{String.valueOf(albumId)});
        return photoId;
    }

    public int add(int uid, String privatePath, String publicUri, String title, String category, String message) {
        int albumId = createAlbum(uid, category == null || category.isEmpty() ? "Other" : category);
        return addPhoto(uid, albumId, privatePath, publicUri, title, category == null || category.isEmpty() ? "Other" : category, message);
    }

    public void delete(int id) {
        h.getWritableDatabase().delete("album_photos", "id=?", new String[]{String.valueOf(id)});
    }

    public int readTodayCount(int userId) {
        Cursor c = h.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM album_photos WHERE user_id=? AND date(uploaded_at,'localtime')=date('now','localtime')",
            new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public void setFavorite(int id, boolean favorite) {
        ContentValues v = new ContentValues();
        v.put("favorite", favorite ? 1 : 0);
        h.getWritableDatabase().update("album_photos", v, "id=?", new String[]{String.valueOf(id)});
    }
}