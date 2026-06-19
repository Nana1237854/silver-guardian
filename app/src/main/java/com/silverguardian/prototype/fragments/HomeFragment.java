package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;

public class HomeFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        bindHero(root);
        bindSummary(root);
        bindShortcuts(root);
        bindAiCard(root);
        return root;
    }

    private void bindHero(View root) {
        ((ImageView) root.findViewById(R.id.home_hero_image))
            .setContentDescription(getString(R.string.home_greeting_title));
        ((TextView) root.findViewById(R.id.home_hero_title)).setText(R.string.home_greeting_title);
        ((TextView) root.findViewById(R.id.home_hero_subtitle)).setText(R.string.home_greeting_subtitle);
        ((TextView) root.findViewById(R.id.home_hero_status)).setText(R.string.home_greeting_status);
    }

    private void bindSummary(View root) {
        ((TextView) root.findViewById(R.id.home_summary_title)).setText(R.string.home_summary_title_refined);
        ((TextView) root.findViewById(R.id.home_summary_time)).setText(R.string.home_summary_time_refined);

        bindMetric(root.findViewById(R.id.home_metric_blood_pressure), "120/76", getString(R.string.home_metric_pressure_label));
        bindMetric(root.findViewById(R.id.home_metric_heart_rate), "72", getString(R.string.home_metric_heart_label));
        bindMetric(root.findViewById(R.id.home_metric_blood_oxygen), "98%", getString(R.string.home_metric_oxygen_label));
        bindMetric(root.findViewById(R.id.home_metric_sleep), "7.5h", getString(R.string.home_metric_sleep_label));
    }

    private void bindShortcuts(View root) {
        bindShortcut(root.findViewById(R.id.home_health_action), R.drawable.ic_health,
            getString(R.string.home_shortcut_health_title), getString(R.string.home_shortcut_health_subtitle),
            v -> main().openHealth());
        bindShortcut(root.findViewById(R.id.home_medicine_action), R.drawable.ic_medicine,
            getString(R.string.home_shortcut_medicine_title), getString(R.string.home_shortcut_medicine_subtitle),
            v -> main().openMedicine());
        bindShortcut(root.findViewById(R.id.home_album_action), R.drawable.ic_album,
            getString(R.string.home_shortcut_album_title), getString(R.string.home_shortcut_album_subtitle),
            v -> main().openAlbum());
        bindShortcut(root.findViewById(R.id.home_settings_action), R.drawable.ic_settings,
            getString(R.string.home_shortcut_settings_title), getString(R.string.home_shortcut_settings_subtitle),
            v -> main().openSettings());
    }

    private void bindAiCard(View root) {
        ((ImageView) root.findViewById(R.id.home_ai_image))
            .setContentDescription(getString(R.string.home_ai_title_refined));
        ((TextView) root.findViewById(R.id.home_ai_title)).setText(R.string.home_ai_title_refined);
        ((TextView) root.findViewById(R.id.home_ai_subtitle)).setText(R.string.home_ai_subtitle_refined);
        ((TextView) root.findViewById(R.id.home_ai_cta)).setText(R.string.home_ai_cta_refined);
        root.findViewById(R.id.home_ai_action).setOnClickListener(
            v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
    }

    private void bindMetric(View metricRoot, String value, String label) {
        ((TextView) metricRoot.findViewById(R.id.home_metric_value)).setText(value);
        ((TextView) metricRoot.findViewById(R.id.home_metric_label)).setText(label);
    }

    private void bindShortcut(View shortcutRoot, int iconRes, String title, String subtitle,
                              View.OnClickListener clickListener) {
        ImageView icon = shortcutRoot.findViewById(R.id.home_shortcut_icon);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color(R.color.primary_dark));
        ((TextView) shortcutRoot.findViewById(R.id.home_shortcut_title)).setText(title);
        ((TextView) shortcutRoot.findViewById(R.id.home_shortcut_subtitle)).setText(subtitle);
        shortcutRoot.setOnClickListener(clickListener);
    }

    private MainActivity main() {
        return (MainActivity) requireActivity();
    }
}
