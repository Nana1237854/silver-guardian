package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 防诈骗知识详情页 — 展示案例与防范措施。
 */
public class FraudDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra("fraud_title");
        String category = getIntent().getStringExtra("fraud_category");
        String detail = getIntent().getStringExtra("fraud_detail");
        String measures = getIntent().getStringExtra("fraud_measures");
        if (title == null) title = "防诈骗知识";
        if (detail == null) detail = "";
        if (measures == null) measures = "";

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(48));
        scroll.addView(root);

        // 分类标签
        TextView cat = new TextView(this);
        cat.setText(category);
        cat.setTextSize(14);
        cat.setTextColor(getColor(R.color.primary));
        cat.setBackgroundResource(R.drawable.bg_tag);
        cat.setPadding(dp(12), dp(6), dp(12), dp(6));
        root.addView(cat);

        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(26);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(getColor(R.color.text_primary));
        titleView.setPadding(0, dp(16), 0, dp(16));
        root.addView(titleView);

        // 详情
        TextView detailView = new TextView(this);
        detailView.setText("📋 案例详情\n\n" + detail);
        detailView.setTextSize(18);
        detailView.setTextColor(getColor(R.color.text_primary));
        detailView.setLineSpacing(dp(4), 1f);
        detailView.setPadding(0, 0, 0, dp(24));
        root.addView(detailView);

        // 分隔线
        View divider = new View(this);
        divider.setBackgroundColor(getColor(R.color.divider));
        root.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        // 防范措施
        TextView measuresTitle = new TextView(this);
        measuresTitle.setText("🛡️ 防范措施");
        measuresTitle.setTextSize(20);
        measuresTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        measuresTitle.setTextColor(getColor(R.color.text_primary));
        measuresTitle.setPadding(0, dp(24), 0, dp(12));
        root.addView(measuresTitle);

        for (String measure : measures.split("\n")) {
            String m = measure.trim();
            if (m.isEmpty()) continue;
            TextView item = new TextView(this);
            item.setText("• " + m);
            item.setTextSize(17);
            item.setTextColor(getColor(R.color.text_primary));
            item.setPadding(0, 0, 0, dp(8));
            item.setLineSpacing(dp(2), 1f);
            root.addView(item);
        }

        setContentView(scroll);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
