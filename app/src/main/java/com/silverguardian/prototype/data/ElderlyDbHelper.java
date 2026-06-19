package com.silverguardian.prototype.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper for the elderly_guardian.db schema.
 * Extracted into a standalone file so database setup can be tested and reused independently.
 */
public class ElderlyDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "elderly_guardian.db";
    private static final int DB_VERSION = 1;

    public ElderlyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Test-only constructor that allows overriding database name and version. */
    ElderlyDbHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, pin TEXT NOT NULL, avatar TEXT, age INTEGER, health_conditions TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))");
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, content TEXT NOT NULL, type TEXT NOT NULL CHECK(type IN ('user','ai')), created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE health_data (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, type TEXT NOT NULL, value TEXT NOT NULL, notes TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, memory TEXT NOT NULL, category TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, message TEXT NOT NULL, reminder_time TEXT NOT NULL, triggered INTEGER NOT NULL DEFAULT 0, reminder_type TEXT DEFAULT 'medicine', description TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE family_members (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, relationship TEXT NOT NULL, phone TEXT, avatar TEXT, status TEXT DEFAULT 'offline', created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE user_medicines (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, type TEXT, time TEXT, method TEXT, description TEXT, image TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE medicine_taken (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, medicine_id INTEGER NOT NULL, taken_date TEXT NOT NULL, taken_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), UNIQUE(user_id, medicine_id, taken_date), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(medicine_id) REFERENCES user_medicines(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE album_photos (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, url TEXT NOT NULL, title TEXT NOT NULL, description TEXT, category TEXT DEFAULT 'family', uploaded_by TEXT DEFAULT 'Family', favorite INTEGER DEFAULT 0, scene_tag TEXT, family_message TEXT, uploaded_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE emergency_alerts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, keyword TEXT NOT NULL, message TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE medicine_library (id INTEGER PRIMARY KEY AUTOINCREMENT, disease TEXT NOT NULL, medicine_name TEXT NOT NULL, brand TEXT, description TEXT, image TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add future migration steps here while preserving user data.
        if (oldVersion < 2) {
            // Example migration slot for a future DB version.
            // db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT");
        }
    }
}