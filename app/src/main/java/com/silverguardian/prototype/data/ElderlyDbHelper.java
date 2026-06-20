package com.silverguardian.prototype.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ElderlyDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "elderly_guardian.db";
    private static final int DB_VERSION = 2;
    public ElderlyDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }
    ElderlyDbHelper(Context context, String dbName, int dbVersion) { super(context, dbName, null, dbVersion); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, pin TEXT NOT NULL, avatar TEXT, age INTEGER, health_conditions TEXT, hint_question TEXT, hint_answer TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))");
        createBusinessTables(db);
    }
    private void createBusinessTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, content TEXT NOT NULL, type TEXT NOT NULL CHECK(type IN ('user','ai')), created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE health_data (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, type TEXT NOT NULL, value TEXT NOT NULL, status TEXT NOT NULL DEFAULT '正常', notes TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE memories (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, memory TEXT NOT NULL, category TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, message TEXT NOT NULL, reminder_time TEXT NOT NULL, triggered INTEGER NOT NULL DEFAULT 0, reminder_type TEXT DEFAULT 'medicine', description TEXT, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE family_members (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, relationship TEXT NOT NULL, phone TEXT, avatar TEXT, status TEXT DEFAULT 'offline', created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE user_medicines (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, type TEXT, time TEXT, method TEXT, description TEXT, image TEXT, advance_minutes INTEGER NOT NULL DEFAULT 0, repeat_count INTEGER NOT NULL DEFAULT 0, repeat_interval INTEGER NOT NULL DEFAULT 10, created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE medicine_taken (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, medicine_id INTEGER NOT NULL, taken_date TEXT NOT NULL, taken_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), UNIQUE(user_id, medicine_id, taken_date), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(medicine_id) REFERENCES user_medicines(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE album_photos (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, url TEXT NOT NULL, public_uri TEXT, title TEXT NOT NULL, description TEXT, category TEXT DEFAULT 'family', uploaded_by TEXT DEFAULT 'Family', favorite INTEGER DEFAULT 0, scene_tag TEXT, family_message TEXT, uploaded_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE emergency_alerts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, keyword TEXT NOT NULL, message TEXT NOT NULL, status TEXT NOT NULL DEFAULT '待处理', created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE medicine_library (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, disease TEXT NOT NULL, medicine_name TEXT NOT NULL, brand TEXT, type TEXT, description TEXT, image TEXT, UNIQUE(user_id, medicine_name), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE fraud_tips (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, content TEXT NOT NULL, action TEXT, UNIQUE(user_id, title), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_health_user_date ON health_data(user_id, created_at)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= 2) return;
        db.execSQL("ALTER TABLE users ADD COLUMN hint_question TEXT");
        db.execSQL("ALTER TABLE users ADD COLUMN hint_answer TEXT");
        String[] tables = {"medicine_taken","messages","health_data","memories","reminders","family_members","user_medicines","album_photos","emergency_alerts","medicine_library","fraud_tips"};
        for (String table : tables) db.execSQL("DROP TABLE IF EXISTS " + table);
        createBusinessTables(db);
        Cursor users = db.rawQuery("SELECT id FROM users", null);
        while (users.moveToNext()) seedDefaultTemplates(db, users.getInt(0));
        users.close();
    }
    public static void seedDefaultTemplates(SQLiteDatabase db, long userId) {
        Object[][] medicines = {
            {"高血压","硝苯地平","拜新同","降压药","用于高血压和心绞痛"},{"高血压","缬沙坦","代文","降压药","用于轻中度高血压"},
            {"糖尿病","二甲双胍","格华止","降糖药","用于二型糖尿病管理"},{"心脑血管","阿司匹林","拜阿司匹灵","抗血小板药","遵医嘱用于心脑血管疾病"},
            {"骨骼健康","碳酸钙 D3","钙尔奇","维生素矿物质","补充钙和维生素 D"},{"感冒发热","对乙酰氨基酚","泰诺","解热镇痛药","用于退热和缓解疼痛"},
            {"咳嗽咳痰","氨溴索","沐舒坦","祛痰药","帮助稀释痰液"},{"胃部不适","奥美拉唑","洛赛克","抑酸药","用于胃酸相关不适"}
        };
        for (Object[] i : medicines) db.execSQL("INSERT OR IGNORE INTO medicine_library(user_id,disease,medicine_name,brand,type,description,image) VALUES(?,?,?,?,?,?,?)",new Object[]{userId,i[0],i[1],i[2],i[3],i[4],""});
        Object[][] tips = {
            {"冒充客服退款","电话诈骗","陌生人要求共享屏幕或验证码时立即停止操作。","不要提供验证码"},{"保健品讲座","健康诈骗","免费礼品后高价推销是常见套路，购买前先咨询医生。","先咨询医生"},
            {"冒充亲友借钱","亲情诈骗","转账前通过常用电话号码直接核实身份。","电话核实"},{"中奖先交费","中奖诈骗","要求先交手续费的中奖信息通常是诈骗。","不要转账"}
        };
        for (Object[] i : tips) db.execSQL("INSERT OR IGNORE INTO fraud_tips(user_id,title,category,content,action) VALUES(?,?,?,?,?)",new Object[]{userId,i[0],i[1],i[2],i[3]});
    }
}
