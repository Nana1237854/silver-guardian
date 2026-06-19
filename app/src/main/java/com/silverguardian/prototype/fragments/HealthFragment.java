package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverguardian.prototype.BluetoothActivity;
import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HealthFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_health, container, false);
        bindHeader(root);
        bindSummary(root);
        bindMetricSection(root);
        bindAnalysisCard(root);
        bindActions(root);
        return root;
    }

    private void bindHeader(View root) {
        ((TextView) root.findViewById(R.id.health_header_title)).setText(R.string.health_title_refined);

        TextView date = root.findViewById(R.id.health_header_date);
        String today = new SimpleDateFormat("M月d日", Locale.CHINESE).format(new Date());
        date.setText(today);
        date.setContentDescription("今天是 " + today);
    }

    private void bindSummary(View root) {
        ImageView image = root.findViewById(R.id.health_summary_image);
        image.setContentDescription(getString(R.string.health_banner_desc_refined));
        ((TextView) root.findViewById(R.id.health_summary_greeting)).setText(R.string.health_greeting_refined);
        ((TextView) root.findViewById(R.id.health_summary_status)).setText(R.string.health_status_refined);
        ((TextView) root.findViewById(R.id.health_summary_detail)).setText(R.string.health_detail_refined);
        ((TextView) root.findViewById(R.id.health_summary_reminder)).setText(R.string.health_reminder_refined);
    }

    private void bindMetricSection(View root) {
        TextView title = root.findViewById(R.id.health_metrics_title);
        title.setText(R.string.health_metrics_title_refined);
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_health, 0, 0, 0);
        title.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.primary)));
        title.setCompoundDrawablePadding(dp(10));

        bindMetricRow(root.findViewById(R.id.health_metric_blood_pressure), R.drawable.ic_health,
            getString(R.string.health_metric_blood_pressure), "128/76", "mmHg", getString(R.string.health_metric_status_normal), false);
        bindMetricRow(root.findViewById(R.id.health_metric_heart_rate), R.drawable.ic_health,
            getString(R.string.health_metric_heart_rate), "72", "次/分", getString(R.string.health_metric_status_normal), false);
        bindMetricRow(root.findViewById(R.id.health_metric_blood_oxygen), R.drawable.ic_breathe,
            getString(R.string.bluetooth_type_blood_oxygen), "98", "%", getString(R.string.health_metric_status_normal), false);
        bindMetricRow(root.findViewById(R.id.health_metric_sleep), R.drawable.ic_sleep,
            getString(R.string.health_metric_sleep), "7小时20分", "", getString(R.string.health_metric_status_good_sleep), true);
    }

    private void bindMetricRow(View row, int iconRes, String label, String value, String unit,
                               String status, boolean warmStatus) {
        ImageView icon = row.findViewById(R.id.health_metric_icon);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color(R.color.primary));
        icon.setContentDescription(null);

        ((TextView) row.findViewById(R.id.health_metric_title)).setText(label);
        ((TextView) row.findViewById(R.id.health_metric_value)).setText(value);

        TextView unitView = row.findViewById(R.id.health_metric_unit);
        if (unit == null || unit.isEmpty()) {
            unitView.setVisibility(View.GONE);
        } else {
            unitView.setVisibility(View.VISIBLE);
            unitView.setText(unit);
        }

        TextView badge = row.findViewById(R.id.health_metric_status);
        badge.setText(status);
        badge.setTextColor(warmStatus ? color(R.color.accent_orange_dark) : color(R.color.status_good));
        badge.setBackgroundResource(warmStatus ? R.drawable.bg_status_warm : R.drawable.bg_status_good);

        row.setContentDescription(label + value + unit + status);
        row.setOnClickListener(v -> toast(getString(R.string.health_metric_view_record, label)));
    }

    private void bindAnalysisCard(View root) {
        View card = root.findViewById(R.id.health_analysis_card);
        ((ImageView) card.findViewById(R.id.health_ai_image)).setContentDescription(getString(R.string.health_analysis_title_refined));
        ((TextView) card.findViewById(R.id.health_ai_title)).setText(R.string.health_analysis_title_refined);
        ((TextView) card.findViewById(R.id.health_ai_body)).setText(R.string.health_analysis_body_refined);
        ((TextView) card.findViewById(R.id.health_ai_action)).setText(R.string.health_analysis_action_refined);
        card.setContentDescription(getString(R.string.health_analysis_desc_refined));
        card.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
    }

    private void bindActions(View root) {
        ((TextView) root.findViewById(R.id.health_actions_title)).setText(R.string.health_record_title_refined);

        TextView bluetooth = root.findViewById(R.id.health_action_bluetooth);
        bluetooth.setText(R.string.health_bluetooth_action_refined);
        bluetooth.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bluetooth, 0, 0, 0);
        bluetooth.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.surface_white)));
        bluetooth.setCompoundDrawablePadding(dp(7));
        bluetooth.setOnClickListener(v -> startActivity(new Intent(requireContext(), BluetoothActivity.class)));

        TextView manual = root.findViewById(R.id.health_action_manual);
        manual.setText(R.string.health_manual_action_refined);
        manual.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0);
        manual.setCompoundDrawableTintList(ColorStateList.valueOf(color(R.color.primary_dark)));
        manual.setCompoundDrawablePadding(dp(7));
        manual.setOnClickListener(v -> {
            MockData.addHealthData("heart_rate", "69", getString(R.string.health_metric_status_normal));
            toast(getString(R.string.health_manual_success_refined));
        });
    }
}
