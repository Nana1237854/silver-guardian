package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FraudDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_detail);

        String title = getIntent().getStringExtra("fraud_title");
        String category = getIntent().getStringExtra("fraud_category");
        String detail = getIntent().getStringExtra("fraud_detail");
        String measures = getIntent().getStringExtra("fraud_measures");

        bindHeader();
        bindContent(
            title == null || title.trim().isEmpty() ? getString(R.string.fraud_detail_default_title) : title,
            category == null ? "" : category,
            detail == null ? "" : detail,
            measures == null ? "" : measures
        );
    }

    private void bindHeader() {
        View header = findViewById(R.id.fraud_detail_header);
        TextView back = header.findViewById(R.id.header_back);
        TextView title = header.findViewById(R.id.header_title);
        TextView action = header.findViewById(R.id.header_action);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        title.setText(R.string.fraud_detail_default_title);
        action.setVisibility(View.GONE);
    }

    private void bindContent(String titleText, String category, String detail, String measures) {
        TextView categoryView = findViewById(R.id.fraud_detail_category);
        TextView titleView = findViewById(R.id.fraud_detail_title);
        View caseSection = findViewById(R.id.fraud_detail_case_section);
        TextView caseTitle = caseSection.findViewById(R.id.section_card_title);
        LinearLayout caseContent = caseSection.findViewById(R.id.section_card_content);
        View measuresSection = findViewById(R.id.fraud_detail_measures_section);
        TextView measuresTitle = measuresSection.findViewById(R.id.section_card_title);
        LinearLayout measuresContainer = measuresSection.findViewById(R.id.section_card_content);

        if (category.trim().isEmpty()) {
            categoryView.setVisibility(View.GONE);
        } else {
            categoryView.setText(category);
        }

        titleView.setText(titleText);
        caseTitle.setText(R.string.fraud_detail_case_title);
        measuresTitle.setText(R.string.fraud_detail_measures_title);

        caseContent.removeAllViews();
        View body = LayoutInflater.from(this).inflate(R.layout.view_detail_body_text, caseContent, false);
        ((TextView) body.findViewById(R.id.detail_body_text)).setText(detail);
        caseContent.addView(body);

        measuresContainer.removeAllViews();
        for (String measure : measures.split("\\n")) {
            String item = measure.trim();
            if (item.isEmpty()) {
                continue;
            }
            View row = LayoutInflater.from(this).inflate(R.layout.view_detail_bullet_row, measuresContainer, false);
            TextView measureView = row.findViewById(R.id.detail_bullet_text);
            measureView.setText(getString(R.string.detail_bullet_prefix, item));
            measuresContainer.addView(row);
        }
    }
}
