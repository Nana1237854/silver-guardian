package com.silverguardian.prototype.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ElderlyDbHelperMigrationTest {

    private static final String TEST_DB = "elderly_guardian_test.db";
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(TEST_DB);
    }

    @After
    public void tearDown() {
        context.deleteDatabase(TEST_DB);
    }

    @Test
    public void upgradePreservesUserData() {
        // 1. v1: 创建数据库并插入测试数据
        ElderlyDbHelper v1Helper = new ElderlyDbHelper(context, TEST_DB, 1);
        SQLiteDatabase db = v1Helper.getWritableDatabase();

        ContentValues userValues = new ContentValues();
        userValues.put("name", "测试老人");
        userValues.put("pin", "8888");
        userValues.put("age", 75);
        db.insert("users", null, userValues);

        ContentValues healthValues = new ContentValues();
        healthValues.put("user_id", 1);
        healthValues.put("type", "heart_rate");
        healthValues.put("value", "72");
        db.insert("health_data", null, healthValues);

        ContentValues medicineValues = new ContentValues();
        medicineValues.put("user_id", 1);
        medicineValues.put("name", "阿司匹林");
        medicineValues.put("type", "西药");
        medicineValues.put("time", "08:00");
        db.insert("user_medicines", null, medicineValues);

        db.close();
        v1Helper.close();

        // 2. v2: 用更高版本号打开同一数据库，触发 onUpgrade
        ElderlyDbHelper v2Helper = new ElderlyDbHelper(context, TEST_DB, 2);
        SQLiteDatabase upgradedDb = v2Helper.getWritableDatabase();

        // 3. 用户数据完整保留
        Cursor userCursor = upgradedDb.rawQuery(
            "SELECT * FROM users WHERE name = ?", new String[]{"测试老人"});
        assertTrue("v1→v2 升级后用户数据应保留", userCursor.moveToFirst());
        assertEquals("8888", userCursor.getString(userCursor.getColumnIndex("pin")));
        assertEquals(75, userCursor.getInt(userCursor.getColumnIndex("age")));
        userCursor.close();

        // 4. 健康数据完整保留
        Cursor healthCursor = upgradedDb.rawQuery(
            "SELECT * FROM health_data WHERE type = ?", new String[]{"heart_rate"});
        assertTrue("v1→v2 升级后健康数据应保留", healthCursor.moveToFirst());
        assertEquals("72", healthCursor.getString(healthCursor.getColumnIndex("value")));
        healthCursor.close();

        // 5. 用药数据完整保留
        Cursor medCursor = upgradedDb.rawQuery(
            "SELECT * FROM user_medicines WHERE name = ?", new String[]{"阿司匹林"});
        assertTrue("v1→v2 升级后用药数据应保留", medCursor.moveToFirst());
        medCursor.close();

        upgradedDb.close();
        v2Helper.close();
    }
}
