package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 防诈骗知识详情页。
 * 分块展示：骗局简介、案例详情、风险说明、防范措施、内容来源。
 * 适合老年人阅读：大字号、分块清晰、层级明确。
 */
public class FraudDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fraud_detail);

        String title = getIntent().getStringExtra("fraud_title");
        String category = getIntent().getStringExtra("fraud_category");
        String summary = getIntent().getStringExtra("fraud_summary");
        String detail = getIntent().getStringExtra("fraud_detail");
        String risk = getIntent().getStringExtra("fraud_risk");
        String advice = getIntent().getStringExtra("fraud_advice");
        String sourceName = getIntent().getStringExtra("fraud_source_name");
        String sourceType = getIntent().getStringExtra("fraud_source_type");
        String sourceDate = getIntent().getStringExtra("fraud_source_date");
        boolean isRemote = getIntent().getBooleanExtra("fraud_is_remote", false);

        bindHeader();
        bindContent(
            title == null || title.trim().isEmpty() ? getString(R.string.fraud_detail_default_title) : title,
            category != null ? category : "",
            summary != null ? summary : "",
            detail != null ? detail : "",
            risk != null ? risk : "",
            advice != null ? advice : "",
            sourceName != null ? sourceName : "",
            sourceType != null ? sourceType : "",
            sourceDate != null ? sourceDate : "",
            isRemote
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

    private void bindContent(String titleText, String category, String summary,
                             String detail, String risk, String advice,
                             String sourceName, String sourceType, String sourceDate,
                             boolean isRemote) {
        // 离线提示：view_status_strip 根视图是 TextView
        TextView offlineHint = findViewById(R.id.fraud_detail_offline_hint);
        if (isRemote) {
            offlineHint.setVisibility(View.GONE);
        } else {
            offlineHint.setVisibility(View.VISIBLE);
            offlineHint.setText(R.string.fraud_offline_hint_detail);
        }

        // 分类标签
        TextView categoryView = findViewById(R.id.fraud_detail_category);
        if (category.trim().isEmpty()) {
            categoryView.setVisibility(View.GONE);
        } else {
            categoryView.setText(category);
        }

        // 标题
        TextView titleView = findViewById(R.id.fraud_detail_title);
        titleView.setText(titleText);

        // 1) 骗局简介
        bindTextSection(R.id.fraud_detail_summary_section,
            R.string.fraud_detail_summary_title, summary);

        // 2) 案例详情
        bindTextSection(R.id.fraud_detail_case_section,
            R.string.fraud_detail_case_title, detail);

        // 3) 风险说明
        bindTextSection(R.id.fraud_detail_risk_section,
            R.string.fraud_detail_risk_title, risk);

        // 4) 防范措施（分条展示）
        bindBulletSection(R.id.fraud_detail_advice_section,
            R.string.fraud_detail_advice_title, advice);

        // 5) 内容来源
        bindSourceSection(R.id.fraud_detail_source_section,
            sourceName, sourceType, sourceDate);
    }

    /** 纯文本区块 */
    private void bindTextSection(int sectionId, int titleRes, String text) {
        View section = findViewById(sectionId);
        if (text == null || text.trim().isEmpty()) {
            section.setVisibility(View.GONE);
            return;
        }
        TextView sectionTitle = section.findViewById(R.id.section_card_title);
        sectionTitle.setText(titleRes);
        LinearLayout content = section.findViewById(R.id.section_card_content);
        content.removeAllViews();
        View body = LayoutInflater.from(this).inflate(R.layout.view_detail_body_text, content, false);
        ((TextView) body.findViewById(R.id.detail_body_text)).setText(text.trim());
        content.addView(body);
    }

    /** 分条列表区块，适用于防范措施 */
    private void bindBulletSection(int sectionId, int titleRes, String text) {
        View section = findViewById(sectionId);
        if (text == null || text.trim().isEmpty()) {
            section.setVisibility(View.GONE);
            return;
        }
        TextView sectionTitle = section.findViewById(R.id.section_card_title);
        sectionTitle.setText(titleRes);
        LinearLayout content = section.findViewById(R.id.section_card_content);
        content.removeAllViews();
        for (String line : text.split("\\n")) {
            String item = line.trim();
            if (item.isEmpty()) {
                continue;
            }
            View row = LayoutInflater.from(this).inflate(R.layout.view_detail_bullet_row, content, false);
            TextView bulletView = row.findViewById(R.id.detail_bullet_text);
            bulletView.setText(getString(R.string.detail_bullet_prefix, item));
            content.addView(row);
        }
    }

    /** 内容来源区块 */
    private void bindSourceSection(int sectionId, String sourceName, String sourceType, String sourceDate) {
        View section = findViewById(sectionId);
        if (sourceName == null || sourceName.trim().isEmpty()) {
            section.setVisibility(View.GONE);
            return;
        }
        TextView sectionTitle = section.findViewById(R.id.section_card_title);
        sectionTitle.setText(R.string.fraud_detail_source_title);
        LinearLayout content = section.findViewById(R.id.section_card_content);
        content.removeAllViews();

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.fraud_source_label_name)).append(sourceName);
        if (sourceDate != null && !sourceDate.isEmpty()) {
            sb.append("\n").append(getString(R.string.fraud_source_label_date)).append(sourceDate);
        }
        sb.append("\n").append(getString(R.string.fraud_source_label_type))
            .append("local".equals(sourceType) ? getString(R.string.fraud_source_local) : getString(R.string.fraud_source_official));

        View body = LayoutInflater.from(this).inflate(R.layout.view_detail_body_text, content, false);
        ((TextView) body.findViewById(R.id.detail_body_text)).setText(sb.toString());
        content.addView(body);
    }
}
