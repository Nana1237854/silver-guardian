package com.silverguardian.prototype.fragments;

import android.app.AlertDialog;
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
import com.silverguardian.prototype.models.SafeCheckRecord;
import com.silverguardian.prototype.models.User;
import com.silverguardian.prototype.reminder.SafeCheckScheduler;
import com.silverguardian.prototype.utils.FontScaleHelper;
import com.silverguardian.prototype.weather.WeatherNavigationPlace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private static final int REQUEST_WEATHER_LOCATION = 410;
    private static final int REMIND_LATER_MINUTES = 30;

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
        bindSafeCheck(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null) {
            bindSafeCheck(rootView);
        }
    }

    private void bindCommon(View root) {
        User user = userSession().getActiveUser();
        setText(root, R.id.home_mode_greeting, "您好，" + (user == null ? "长辈" : user.name));
        setText(root, R.id.home_call_action, "一键呼叫");
        setText(root, R.id.home_sos_action, "SOS 紧急呼叫");
        setText(root, R.id.home_health_action, "健康档案");
        setText(root, R.id.home_medicine_action, "用药提醒");
        setText(root, R.id.home_album_action, "家人相册");
        setText(root, R.id.home_community_action, "便民查询");
        setText(root, R.id.home_ai_action, "智能对话");

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
        setText(root, R.id.home_medicine_summary, "今日用药：" + taken + "/" + total + " 已打卡");
    }

    private void bindFull(View root) {
        bindWeather(root);
        User user = userSession().getActiveUser();
        setText(root, R.id.home_hero_title, "早上好，" + (user == null ? "长辈" : user.name));
        setText(root, R.id.home_hero_subtitle, "今天也稳稳照顾自己");
        setText(root, R.id.home_hero_status, "状态平稳");
        setText(root, R.id.home_summary_title, "今日健康概览");
        setText(root, R.id.home_summary_time, "今日最新");
        setText(root, R.id.home_ai_title, "AI 健康分析与建议");
        setText(root, R.id.home_ai_subtitle, "结合近期指标、睡眠和用药节奏，给出更安心的建议。");
        setText(root, R.id.home_ai_cta, "进入 AI 对话");

        Map<String, HealthData> latest = new HashMap<>();
        for (HealthData data : healthRecords().getTodayHealthData()) {
            if (!latest.containsKey(data.type)) {
                latest.put(data.type, data);
            }
        }
        metric(root, R.id.home_metric_blood_pressure, latest.get("blood_pressure"), "血压");
        metric(root, R.id.home_metric_heart_rate, latest.get("heart_rate"), "心率");
        metric(root, R.id.home_metric_blood_oxygen, latest.get("blood_oxygen"), "血氧");
        metric(root, R.id.home_metric_sleep, latest.get("sleep"), "睡眠");

        shortcut(root, R.id.home_health_action, R.drawable.ic_health, "健康档案", "查看指标、报告和每日状态");
        shortcut(root, R.id.home_medicine_action, R.drawable.ic_medicine, "用药提醒", "按时服药，安心完成打卡");
        shortcut(root, R.id.home_album_action, R.drawable.ic_album, "家人相册", "看看家人近况，留住想念");
        shortcut(root, R.id.home_settings_action, R.drawable.ic_settings, "设置", "调整显示、通知和家属协助");
    }

    private void bindSafeCheck(View root) {
        View card = root.findViewById(R.id.home_safe_check_card);
        if (card == null) {
            return;
        }
        TextView title = card.findViewById(R.id.home_safe_check_title);
        TextView desc = card.findViewById(R.id.home_safe_check_desc);
        TextView status = card.findViewById(R.id.home_safe_check_status);
        TextView ok = card.findViewById(R.id.home_safe_check_ok);
        TextView unwell = card.findViewById(R.id.home_safe_check_unwell);
        TextView needFamily = card.findViewById(R.id.home_safe_check_need_family);
        TextView remindLater = card.findViewById(R.id.home_safe_check_remind_later);

        SafeCheckRecord record = safeChecks().getTodayRecord();
        title.setText(record != null && record.isConfirmed()
            ? R.string.safe_check_card_done_title
            : R.string.safe_check_card_title);
        desc.setText(descriptionFor(record));

        if (record == null) {
            status.setVisibility(View.GONE);
        } else {
            status.setVisibility(View.VISIBLE);
            status.setText(getString(R.string.safe_check_status_summary,
                labelFor(record.status),
                record.checkedAt == null || record.checkedAt.isEmpty() ? getString(R.string.safe_check_time_unknown) : record.checkedAt));
        }

        ok.setOnClickListener(v -> {
            safeChecks().saveTodayStatus(SafeCheckRecord.STATUS_OK, getString(R.string.safe_check_status_ok));
            toast(getString(R.string.safe_check_saved_toast));
            bindSafeCheck(root);
        });
        unwell.setOnClickListener(v -> {
            safeChecks().saveTodayStatus(SafeCheckRecord.STATUS_UNWELL, getString(R.string.safe_check_status_unwell));
            toast(getString(R.string.safe_check_unwell_toast));
            bindSafeCheck(root);
        });
        needFamily.setOnClickListener(v -> {
            safeChecks().saveTodayStatus(SafeCheckRecord.STATUS_NEED_FAMILY, getString(R.string.safe_check_status_need_family));
            toast(getString(R.string.safe_check_need_family_toast));
            bindSafeCheck(root);
            main().showOneTapChooser();
        });
        remindLater.setOnClickListener(v -> {
            safeChecks().saveTodayStatus(SafeCheckRecord.STATUS_REMIND_LATER, getString(R.string.safe_check_status_remind_later));
            SafeCheckScheduler.scheduleDelayedReminder(requireContext(), userSession().getActiveUserId(), REMIND_LATER_MINUTES);
            toast(getString(R.string.safe_check_remind_later_toast));
            bindSafeCheck(root);
        });
    }

    private String descriptionFor(SafeCheckRecord record) {
        if (record == null) {
            return getString(R.string.safe_check_card_desc);
        }
        if (SafeCheckRecord.STATUS_OK.equals(record.status)) {
            return getString(R.string.safe_check_desc_ok);
        }
        if (SafeCheckRecord.STATUS_UNWELL.equals(record.status)) {
            return getString(R.string.safe_check_desc_unwell);
        }
        if (SafeCheckRecord.STATUS_NEED_FAMILY.equals(record.status)) {
            return getString(R.string.safe_check_desc_need_family);
        }
        if (SafeCheckRecord.STATUS_REMIND_LATER.equals(record.status)) {
            return getString(R.string.safe_check_desc_remind_later);
        }
        if (SafeCheckRecord.STATUS_MISSED.equals(record.status)) {
            return getString(R.string.safe_check_desc_missed);
        }
        return getString(R.string.safe_check_card_desc);
    }

    private String labelFor(String status) {
        if (SafeCheckRecord.STATUS_OK.equals(status)) {
            return getString(R.string.safe_check_status_ok);
        }
        if (SafeCheckRecord.STATUS_UNWELL.equals(status)) {
            return getString(R.string.safe_check_status_unwell);
        }
        if (SafeCheckRecord.STATUS_NEED_FAMILY.equals(status)) {
            return getString(R.string.safe_check_status_need_family);
        }
        if (SafeCheckRecord.STATUS_REMIND_LATER.equals(status)) {
            return getString(R.string.safe_check_status_remind_later);
        }
        if (SafeCheckRecord.STATUS_MISSED.equals(status)) {
            return getString(R.string.safe_check_status_missed);
        }
        return getString(R.string.safe_check_card_title);
    }

    private void bindWeather(View root) {
        TextView summary = root.findViewById(R.id.home_weather_summary);
        TextView advice = root.findViewById(R.id.home_weather_advice);
        TextView navBtn = root.findViewById(R.id.home_weather_nav_btn);
        View weatherCard = root.findViewById(R.id.home_weather_card);
        if (summary == null || advice == null || weatherCard == null || navBtn == null) return;
        weatherCard.setOnClickListener(v -> openWeatherNavigation("医院"));
        summary.setText("正在定位并获取天气…");
        advice.setText("将综合天气、温度和空气质量生成出行建议。");
        navBtn.setVisibility(View.GONE);
        navBtn.setOnClickListener(null);
        if (!weather().hasLocationPermission() && !locationPermissionRequested) {
            locationPermissionRequested = true;
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_WEATHER_LOCATION);
        }
        weather().load(report -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                summary.setText(report.summary);
                advice.setText(report.advice);
                if (report.recommendedPlaces.isEmpty()) {
                    navBtn.setVisibility(View.GONE);
                    navBtn.setOnClickListener(null);
                } else {
                    navBtn.setVisibility(View.VISIBLE);
                    navBtn.setOnClickListener(v -> showWeatherNavigationDialog(report.recommendedPlaces));
                }
                weatherCard.setOnClickListener(v -> openWeatherNavigation(report.suitableForOuting ? "公园" : "医院"));
            });
        });
    }

    private void showWeatherNavigationDialog(List<WeatherNavigationPlace> places) {
        if (places == null || places.isEmpty()) {
            toast("暂无可导航地点");
            return;
        }
        String[] items = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            WeatherNavigationPlace place = places.get(i);
            String distanceText = place.distanceText == null || place.distanceText.trim().isEmpty() ? "未知" : place.distanceText;
            items[i] = place.name + " 距离您 " + distanceText;
        }
        new AlertDialog.Builder(requireContext())
            .setTitle("选择导航地点")
            .setItems(items, (dialog, which) -> startWeatherNavigation(places.get(which)))
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void startWeatherNavigation(WeatherNavigationPlace place) {
        if (place == null || place.poiItem == null) {
            toast("导航地点暂不可用");
            return;
        }
        if (!communitySearch().openNavigation(requireContext(), place.poiItem)) {
            new AlertDialog.Builder(requireContext())
                .setTitle("未安装高德地图")
                .setMessage("是否使用网页步行导航？")
                .setPositiveButton(R.string.common_ok, (dialog, which) -> communitySearch().openWebNavigation(requireContext(), place.poiItem))
                .setNegativeButton(R.string.common_cancel, null)
                .show();
        }
    }

    private void openWeatherNavigation(String keyword) {
        startActivity(new Intent(requireContext(), CommunityActivity.class).putExtra("initial_keyword", keyword));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
