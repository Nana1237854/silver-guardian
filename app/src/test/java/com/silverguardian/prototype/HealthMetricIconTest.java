package com.silverguardian.prototype;

import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HealthMetricIconTest {
    @Test public void todayHealthOverviewBindsAllFourMetricIcons() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class).putExtra("initial_tab", "health");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().executePendingTransactions();
                assertIcon(activity.findViewById(R.id.health_metric_blood_pressure));
                assertIcon(activity.findViewById(R.id.health_metric_heart_rate));
                assertIcon(activity.findViewById(R.id.health_metric_blood_oxygen));
                assertIcon(activity.findViewById(R.id.health_metric_sleep));
            });
        }
    }

    private static void assertIcon(View row) {
        assertNotNull(row);
        ImageView icon = row.findViewById(R.id.health_metric_icon);
        assertNotNull(icon);
        assertNotNull(icon.getDrawable());
    }
}