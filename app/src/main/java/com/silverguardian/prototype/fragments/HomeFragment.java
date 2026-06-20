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
import com.silverguardian.prototype.CommunityActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.User;
import com.silverguardian.prototype.utils.FontScaleHelper;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private static final int REQUEST_WEATHER_LOCATION = 410;
    private View rootView;
    private boolean locationPermissionRequested;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        int layout = FontScaleHelper.isMinimalMode(requireContext())
            ? R.layout.fragment_home_minimal
            : FontScaleHelper.isSimplifiedMode(requireContext())
                ? R.layout.fragment_home_simplified
                : R.layout.fragment_home;
        View root = inflater.inflate(layout, container, false);
        rootView = root;
        bindCommon(root);
        if (layout == R.layout.fragment_home) {
            bindFull(root);
        }
        return root;
    }

    private void bindCommon(View root) {
        User user = userSession().getActiveUser();
        setText(root, R.id.home_mode_greeting, "\u60a8\u597d\uff0c" + (user == null ? "\u8001\u4eba\u5bb6" : user.name));
        setText(root, R.id.home_call_action, "\u4e00\u952e\u547c\u53eb");
        setText(root, R.id.home_sos_action, "SOS \u7d27\u6025\u547c\u53eb");
        setText(root, R.id.home_health_action, "\u5065\u5eb7\u6863\u6848");
        setText(root, R.id.home_medicine_action, "\u7528\u836f\u63d0\u9192");
        setText(root, R.id.home_album_action, "\u5bb6\u4eba\u76f8\u518c");
        setText(root, R.id.home_community_action, "\u4fbf\u6c11\u67e5\u8be2");
        setText(root, R.id.home_ai_action, "\u667a\u80fd\u5bf9\u8bdd");

        click(root, R.id.home_call_action, view -> main().showOneTapChooser());
        click(root, R.id.home_sos_action, view -> main().showSosDialog());
        click(root, R.id.home_health_action, view -> main().openHealth());
        click(root, R.id.home_medicine_action, view -> main().openMedicine());
        click(root, R.id.home_album_action, view -> main().openAlbum());
        click(root, R.id.home_settings_action, view -> main().openSettings());
        click(root, R.id.home_community_action, view -> startActivity(new Intent(requireContext(), CommunityActivity.class)));
        click(root, R.id.home_ai_action, view -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));

        int total = medicineReminders().getMedicines().size();
        int taken = medicineReminders().getTakenCount();
        setText(root, R.id.home_medicine_summary, "\u4eca\u65e5\u7528\u836f\uff1a" + taken + "/" + total + " \u5df2\u6253\u5361");
    }

    private void bindFull(View root) {
        bindWeather(root);
        User user = userSession().getActiveUser();
        setText(root, R.id.home_hero_title, "\u65e9\u4e0a\u597d\uff0c" + (user == null ? "\u8001\u4eba\u5bb6" : user.name));
        setText(root, R.id.home_hero_subtitle, "\u4eca\u5929\u4e5f\u7a33\u7a33\u7167\u987e\u81ea\u5df1");
        setText(root, R.id.home_hero_status, "\u72b6\u6001\u5e73\u7a33");
        setText(root, R.id.home_summary_title, "\u4eca\u65e5\u5065\u5eb7\u6982\u89c8");
        setText(root, R.id.home_summary_time, "\u4eca\u65e5\u6700\u65b0");
        setText(root, R.id.home_ai_title, "AI \u5065\u5eb7\u5206\u6790\u4e0e\u5efa\u8bae");
        setText(root, R.id.home_ai_subtitle, "\u7ed3\u5408\u8fd1\u671f\u6307\u6807\u3001\u7761\u7720\u548c\u7528\u836f\u8282\u594f\uff0c\u7ed9\u51fa\u66f4\u5b89\u5fc3\u7684\u5efa\u8bae\u3002");
        setText(root, R.id.home_ai_cta, "\u8fdb\u5165 AI \u5bf9\u8bdd");

        Map<String, HealthData> latest = new HashMap<>();
        for (HealthData data : healthRecords().getTodayHealthData()) {
            if (!latest.containsKey(data.type)) {
                latest.put(data.type, data);
            }
        }
        metric(root, R.id.home_metric_blood_pressure, latest.get("blood_pressure"), "\u8840\u538b");
        metric(root, R.id.home_metric_heart_rate, latest.get("heart_rate"), "\u5fc3\u7387");
        metric(root, R.id.home_metric_blood_oxygen, latest.get("blood_oxygen"), "\u8840\u6c27");
        metric(root, R.id.home_metric_sleep, latest.get("sleep"), "\u7761\u7720");

        shortcut(root, R.id.home_health_action, R.drawable.ic_health, "\u5065\u5eb7\u6863\u6848", "\u67e5\u770b\u6307\u6807\u3001\u62a5\u544a\u548c\u6bcf\u65e5\u72b6\u6001");
        shortcut(root, R.id.home_medicine_action, R.drawable.ic_medicine, "\u7528\u836f\u63d0\u9192", "\u6309\u65f6\u670d\u836f\uff0c\u5b89\u5fc3\u5b8c\u6210\u6253\u5361");
        shortcut(root, R.id.home_album_action, R.drawable.ic_album, "\u5bb6\u4eba\u76f8\u518c", "\u770b\u770b\u5bb6\u4eba\u8fd1\u51b5\uff0c\u7559\u4f4f\u60f3\u5ff5");
        shortcut(root, R.id.home_settings_action, R.drawable.ic_settings, "\u8bbe\u7f6e", "\u8c03\u6574\u663e\u793a\u3001\u901a\u77e5\u548c\u5bb6\u5c5e\u534f\u52a9");
    }

    private void bindWeather(View root) {
        TextView summary = root.findViewById(R.id.home_weather_summary);
        TextView advice = root.findViewById(R.id.home_weather_advice);
        View weatherCard = root.findViewById(R.id.home_weather_card);
        if (summary == null || advice == null || weatherCard == null) return;
        weatherCard.setOnClickListener(v -> openWeatherNavigation("医院"));
        summary.setText("正在定位并获取天气…");
        advice.setText("将综合天气、温度和空气质量生成出行建议。");
        if (!weather().hasLocationPermission() && !locationPermissionRequested) {
            locationPermissionRequested = true;
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_WEATHER_LOCATION);
        }
        weather().load(report -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                summary.setText(report.summary);
                advice.setText(report.advice);
                weatherCard.setOnClickListener(v -> openWeatherNavigation(report.suitableForOuting ? "公园" : "医院"));
            });
        });
    }

    private void openWeatherNavigation(String keyword) {
        startActivity(new Intent(requireContext(), CommunityActivity.class).putExtra("initial_keyword", keyword));
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WEATHER_LOCATION && rootView != null) bindWeather(rootView);
    }
    private void metric(View root, int rowId, HealthData data, String label) {
        View row = root.findViewById(rowId);
        if (row == null) {
            return;
        }
        TextView value = row.findViewById(R.id.home_metric_value);
        TextView labelView = row.findViewById(R.id.home_metric_label);
        if (value != null) {
            value.setText(data == null ? "--" : data.value);
        }
        if (labelView != null) {
            labelView.setText(label);
        }
    }

    private void shortcut(View root, int rowId, int iconRes, String title, String subtitle) {
        View row = root.findViewById(rowId);
        if (row == null) {
            return;
        }
        ImageView icon = row.findViewById(R.id.home_shortcut_icon);
        if (icon != null) {
            icon.setImageResource(iconRes);
            icon.setColorFilter(color(R.color.primary_dark));
        }
        TextView titleView = row.findViewById(R.id.home_shortcut_title);
        if (titleView != null) {
            titleView.setText(title);
        }
        TextView subtitleView = row.findViewById(R.id.home_shortcut_subtitle);
        if (subtitleView != null) {
            subtitleView.setText(subtitle);
        }
    }

    private void setText(View root, int id, String text) {
        View view = root.findViewById(id);
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
    }

    private void click(View root, int id, View.OnClickListener listener) {
        View view = root.findViewById(id);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private MainActivity main() {
        return (MainActivity) requireActivity();
    }
}
