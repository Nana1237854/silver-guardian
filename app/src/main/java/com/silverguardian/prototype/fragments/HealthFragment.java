package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverguardian.prototype.BluetoothActivity;
import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.utils.FontScaleHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HealthFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(FontScaleHelper.bgPage(requireContext()));
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(112));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        root.addView(hero());
        root.addView(statusSummary());
        root.addView(metricRow(R.drawable.ic_health, R.drawable.bg_icon_lilac,
            "睡眠", "5小时14分钟", "平均心率 48 bpm", "需关注"));
        root.addView(metricRow(R.drawable.ic_home, R.drawable.bg_icon_coral,
            "运动", "206 千卡", "活动时间 6 分钟", "良好"));
        root.addView(metricRow(R.drawable.ic_album, R.drawable.bg_icon_mint,
            "心情", "良好", "今天 20:10 记录", "稳定"));
        root.addView(metricRow(R.drawable.ic_health, R.drawable.bg_icon_mint,
            "步数", "2265 / 10000 步", "今日目标完成 22%", "继续保持"));
        root.addView(metricRow(R.drawable.ic_medicine, R.drawable.bg_icon_blue,
            "呼吸正念", "0 / 3 分钟", "睡前进行一次轻呼吸", "待完成"));
        root.addView(aiAnalysisCard());
        root.addView(actionBar());
        return scroll;
    }

    private View hero() {
        FrameLayout frame = new FrameLayout(requireContext());
        frame.setBackgroundResource(R.drawable.bg_group_surface);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(184));
        fp.bottomMargin = dp(14);
        frame.setLayoutParams(fp);
        frame.setClipToOutline(true);

        ImageView image = new ImageView(requireContext());
        image.setImageResource(R.drawable.home_companion_header);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setContentDescription("颜爷爷在海边休息的健康陪伴插画");
        frame.addView(image, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(16), dp(16), 0, 0);
        TextView date = text(new SimpleDateFormat("M月d日 EEEE", Locale.CHINESE).format(new Date()), 20, true);
        TextView weather = text("26°C  晴", 16, false);
        weather.setTextColor(color(R.color.text_secondary));
        weather.setPadding(0, dp(7), 0, 0);
        info.addView(date);
        info.addView(weather);
        frame.addView(info, new FrameLayout.LayoutParams(dp(210), -2, Gravity.START | Gravity.TOP));
        return frame;
    }

    private View statusSummary() {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        TextView intro = text("Hi 颜爷爷，今天的状态", 15, false);
        intro.setTextColor(color(R.color.text_secondary));
        card.addView(intro);
        TextView status = text("状态不错", 27, true);
        status.setTextColor(color(R.color.primary_dark));
        status.setPadding(0, dp(4), 0, dp(2));
        card.addView(status);

        TextView detail = text("血压、心率与血氧均在正常范围", 14, false);
        detail.setTextColor(color(R.color.text_secondary));
        card.addView(detail);
        return card;
    }

    private View metricRow(int iconRes, int iconBackground, String label,
                           String value, String meta, String badge) {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setMinimumHeight(dp(92));

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(color(R.color.primary));
        icon.setBackgroundResource(iconBackground);
        icon.setPadding(dp(13), dp(13), dp(13), dp(13));
        icon.setContentDescription(label);
        card.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(15), 0, dp(8), 0);
        info.addView(text(label, 16, true));
        TextView valueView = text(value, 21, true);
        valueView.setPadding(0, dp(2), 0, 0);
        info.addView(valueView);
        TextView metaView = text(meta, 13, false);
        metaView.setTextColor(color(R.color.text_secondary));
        metaView.setPadding(0, dp(3), 0, 0);
        info.addView(metaView);
        card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badgeView = text(badge, 13, true);
        badgeView.setTextColor("需关注".equals(badge) ? color(R.color.health_heart) : color(R.color.status_good));
        badgeView.setBackgroundResource(R.drawable.bg_status_good);
        badgeView.setGravity(Gravity.CENTER);
        card.addView(badgeView);
        return card;
    }

    private View aiAnalysisCard() {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_ai_outline);
        card.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));

        ImageView mascot = new ImageView(requireContext());
        mascot.setImageResource(R.drawable.ai_companion);
        mascot.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mascot.setContentDescription("银发守护 AI 助手");
        card.addView(mascot, new LinearLayout.LayoutParams(dp(68), dp(68)));

        LinearLayout copy = new LinearLayout(requireContext());
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        TextView title = text("AI 健康分析与建议", 18, true);
        title.setTextColor(color(R.color.primary_dark));
        copy.addView(title);
        TextView body = text("综合您的健康数据，获得专属改善建议", 14, false);
        body.setTextColor(color(R.color.text_secondary));
        body.setPadding(0, dp(4), 0, dp(8));
        copy.addView(body);
        TextView action = text("与 AI 对话，深入分析", 15, true);
        action.setGravity(Gravity.CENTER);
        action.setTextColor(color(R.color.surface_white));
        action.setBackgroundResource(R.drawable.bg_button_primary);
        action.setMinHeight(dp(48));
        copy.addView(action, new LinearLayout.LayoutParams(-1, dp(48)));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return card;
    }

    private View actionBar() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, 0);

        TextView call = action("一键呼叫", R.drawable.ic_home, true);
        call.setOnClickListener(v -> ((MainActivity) requireActivity()).oneTapCall());
        call.setOnLongClickListener(v -> {
            ((MainActivity) requireActivity()).oneTapCallLongPress();
            return true;
        });
        row.addView(call, weightedParams(1, 0));

        TextView add = action("添加数据", R.drawable.ic_add, false);
        add.setOnClickListener(v -> {
            MockData.addHealthData("heart_rate", "69", "正常");
            toast("健康数据已添加");
        });
        row.addView(add, weightedParams(1, 8));

        TextView bluetooth = action("蓝牙导入", R.drawable.ic_upload, false);
        bluetooth.setOnClickListener(v -> startActivity(new Intent(requireContext(), BluetoothActivity.class)));
        row.addView(bluetooth, weightedParams(1, 8));
        return row;
    }

    private TextView action(String label, int iconRes, boolean danger) {
        TextView view = text(label, 14, true);
        view.setGravity(Gravity.CENTER);
        view.setMinHeight(dp(52));
        view.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        view.setCompoundDrawablePadding(dp(7));
        view.setTextColor(danger ? color(R.color.sos_red) : color(R.color.primary_dark));
        view.setBackgroundResource(danger ? R.drawable.bg_button_sos : R.drawable.bg_chip_soft);
        if (danger) view.setTextColor(color(R.color.surface_white));
        return view;
    }

    private LinearLayout.LayoutParams weightedParams(int weight, int startMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(52), weight);
        params.leftMargin = dp(startMargin);
        return params;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundResource(R.drawable.bg_group_surface);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        return card;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(sp(size));
        view.setTextColor(FontScaleHelper.textPrimary(requireContext()));
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }
}
