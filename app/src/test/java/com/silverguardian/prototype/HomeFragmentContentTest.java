package com.silverguardian.prototype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.silverguardian.prototype.utils.FontScaleHelper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HomeFragmentContentTest {

    @Test
    public void standardHomeBindsVisibleLabelsAndShortcutIcons() {
        Context context = ApplicationProvider.getApplicationContext();
        FontScaleHelper.setFontMode(context, 0);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().executePendingTransactions();

                assertText(activity, R.id.home_summary_title, "今日健康概览");
                assertMetric(activity, R.id.home_metric_blood_pressure, "血压");
                assertMetric(activity, R.id.home_metric_heart_rate, "心率");
                assertMetric(activity, R.id.home_metric_blood_oxygen, "血氧");
                assertMetric(activity, R.id.home_metric_sleep, "睡眠");
                assertShortcut(activity, R.id.home_health_action, "健康档案");
                assertShortcut(activity, R.id.home_medicine_action, "用药提醒");
                assertShortcut(activity, R.id.home_album_action, "家人相册");
                assertShortcut(activity, R.id.home_settings_action, "设置");
                assertText(activity, R.id.home_ai_title, "AI 健康分析与建议");
            });
        }
    }

    private static void assertText(MainActivity activity, int id, String expected) {
        TextView text = activity.findViewById(id);
        assertNotNull(text);
        assertEquals(expected, text.getText().toString());
    }

    private static void assertMetric(MainActivity activity, int rowId, String expectedLabel) {
        View row = activity.findViewById(rowId);
        assertNotNull(row);
        TextView label = row.findViewById(R.id.home_metric_label);
        assertNotNull(label);
        assertEquals(expectedLabel, label.getText().toString());
    }

    private static void assertShortcut(MainActivity activity, int rowId, String expectedTitle) {
        View row = activity.findViewById(rowId);
        assertNotNull(row);
        TextView title = row.findViewById(R.id.home_shortcut_title);
        assertNotNull(title);
        assertEquals(expectedTitle, title.getText().toString());
        ImageView icon = row.findViewById(R.id.home_shortcut_icon);
        assertNotNull(icon);
        assertNotNull(icon.getDrawable());
    }
}