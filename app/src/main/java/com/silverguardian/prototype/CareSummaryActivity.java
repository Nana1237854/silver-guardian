package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.silverguardian.prototype.models.CareSummary;

// 家属端照护摘要页面：健康/用药/SOS/平安确认汇总展示
public class CareSummaryActivity extends BaseActivity {
    @Override
    // 加载照护摘要页面，绑定汇总数据展示
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_summary);
        bindHeader();
        bindSummary();
    }

    private void bindHeader() {
        View header = findViewById(R.id.care_summary_header);
        TextView back = header.findViewById(R.id.header_back);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.care_summary_title);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindSummary() {
        CareSummary summary = appContainer().careSummaries().getTodaySummary();

        TextView priority = findViewById(R.id.care_summary_priority);
        priority.setText(appContainer().careSummaries().hasPriorityWarning(summary)
            ? R.string.care_summary_priority_warning
            : R.string.care_summary_priority_normal);

        ((TextView) findViewById(R.id.care_summary_overview)).setText(summary.summaryText);
        ((TextView) findViewById(R.id.care_summary_status_value))
            .setText(appContainer().careSummaries().getSafeCheckStatusText(summary.safeCheckStatus));
        ((TextView) findViewById(R.id.care_summary_medicine_value))
            .setText(appContainer().careSummaries().getMedicineText(summary.medicineTotal, summary.medicineTaken));
        ((TextView) findViewById(R.id.care_summary_health_value))
            .setText(appContainer().careSummaries().getHealthText(summary.abnormalHealthCount, summary.abnormalHealthCount == 0 && healthRecords().getTodayHealthData().isEmpty()));
        ((TextView) findViewById(R.id.care_summary_feedback_value))
            .setText(appContainer().careSummaries().getMedicineFeedbackText(summary.medicineFeedbackWarningCount));
        ((TextView) findViewById(R.id.care_summary_alert_value))
            .setText(appContainer().careSummaries().getAlertText(summary.emergencyAlertCount));
        ((TextView) findViewById(R.id.care_summary_family_value))
            .setText(appContainer().careSummaries().getFamilyText(summary.newPhotoCount));
        ((TextView) findViewById(R.id.care_summary_fraud_value))
            .setText(appContainer().careSummaries().getFraudText(summary.fraudTrainingDone));
    }
}
