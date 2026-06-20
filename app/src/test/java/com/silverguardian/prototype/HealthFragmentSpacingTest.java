package com.silverguardian.prototype;

import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.LinearLayout;

import androidx.test.core.app.ActivityScenario;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class HealthFragmentSpacingTest {

    @Test
    public void healthAnalysisKeepsGapAfterTabSwitch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);
                nav.setSelectedItemId(R.id.bottom_health);
                activity.getSupportFragmentManager().executePendingTransactions();
                forceLayout(activity);
                int initialGap = measureGap(activity);

                nav.setSelectedItemId(R.id.bottom_home);
                activity.getSupportFragmentManager().executePendingTransactions();
                forceLayout(activity);

                nav.setSelectedItemId(R.id.bottom_health);
                activity.getSupportFragmentManager().executePendingTransactions();
                forceLayout(activity);
                int switchedGap = measureGap(activity);

                assertTrue("Initial health card gap should be positive, was " + initialGap, initialGap > 0);
                assertTrue("Health card gap should remain positive after switching tabs, was " + switchedGap, switchedGap > 0);
            });
        }
    }

    private static int measureGap(MainActivity activity) {
        View metrics = activity.findViewById(R.id.health_metrics_card);
        View analysis = activity.findViewById(R.id.health_analysis_card);
        return analysis.getTop() - metrics.getBottom();
    }

    private static void forceLayout(MainActivity activity) {
        View content = activity.findViewById(android.R.id.content);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.AT_MOST);
        content.measure(widthSpec, heightSpec);
        content.layout(0, 0, content.getMeasuredWidth(), content.getMeasuredHeight());
        content.requestLayout();
        if (content instanceof LinearLayout) {
            content.invalidate();
        }
    }
}
