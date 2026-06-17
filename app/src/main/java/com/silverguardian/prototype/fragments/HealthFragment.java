package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.silverguardian.prototype.utils.FontScaleHelper;

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
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setBackgroundColor(FontScaleHelper.bgPage(requireContext()));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(110));
        scrollView.addView(root, new ScrollView.LayoutParams(-1, -2));

        root.addView(topIllustration());
        root.addView(greetingCard());
        root.addView(healthMetric("睡眠", "5小时14分钟", "平均心率 48 bpm", "注意"));
        root.addView(healthMetric("运动", "206 千卡", "活动时间 6 分钟", "良好"));
        root.addView(healthMetric("心情", "良好", "今天 20:10 记录", "稳定"));
        root.addView(healthMetric("步数", "2265 /10000 步", "今日目标完成 22%", "继续保持"));
        root.addView(healthMetric("呼吸正念", "0 / 3 分钟", "睡前做一次轻呼吸", "待完成"));
        root.addView(aiCard());
        root.addView(actionBar());
        return scrollView;
    }

    private View topIllustration() {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        card.setBackgroundResource(R.drawable.bg_card_accent);
        TextView date = new TextView(requireContext());
        date.setText(new SimpleDateFormat("M月d日 EEEE", Locale.CHINESE).format(new Date()) + "\n☀ 26°C 晴");
        date.setTextSize(sp(18));
        date.setTextColor(FontScaleHelper.textPrimary(requireContext()));
        date.setGravity(Gravity.CENTER);
        card.addView(date);
        TextView mascot = new TextView(requireContext());
        mascot.setText("●  银发守护助手正在陪伴你");
        mascot.setTextSize(20);
        mascot.setGravity(Gravity.CENTER);
        mascot.setPadding(0, dp(20), 0, 0);
        card.addView(mascot);
        return card;
    }

    private View greetingCard() {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        TextView hi = title("Hi 颜爷爷，今天的状态");
        hi.setTextSize(16);
        hi.setTextColor(getResources().getColor(R.color.text_secondary));
        card.addView(hi);
        TextView state = title("状态不错 ☺");
        state.setTextSize(28);
        card.addView(state);
        return card;
    }

    private View healthMetric(String label, String value, String meta, String badge) {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = new TextView(requireContext());
        icon.setText("●");
        icon.setTextSize(28);
        icon.setTextColor(getResources().getColor(R.color.primary));
        card.addView(icon);

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(16), 0, 0, 0);
        TextView labelView = title(label);
        texts.addView(labelView);
        TextView valueView = title(value);
        valueView.setTextSize(22);
        texts.addView(valueView);
        texts.addView(body(meta));
        card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badgeView = chip(badge);
        card.addView(badgeView);
        return card;
    }

    private View aiCard() {
        LinearLayout card = card();
        card.setBackgroundResource(R.drawable.bg_card_accent);
        card.addView(title("AI 健康分析与建议"));
        card.addView(body("综合你的数据，为你提供专属分析与个人健康建议。"));
        TextView button = chip("与 AI 对话，深入分析  →");
        button.setGravity(Gravity.CENTER);
        button.setTextColor(getResources().getColor(R.color.surface_white));
        button.setBackgroundResource(R.drawable.bg_button_primary);
        button.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
        card.addView(button);
        return card;
    }

    private View actionBar() {
        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(12), 0, 0);

        // 一键呼叫按钮（红色醒目）
        TextView callBtn = chip("📞 一键呼叫");
        callBtn.setTextColor(getResources().getColor(R.color.surface_white));
        callBtn.setBackgroundResource(R.drawable.bg_button_sos);
        callBtn.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).oneTapCall();
            }
        });
        callBtn.setOnLongClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).oneTapCallLongPress();
            }
            return true;
        });
        row.addView(callBtn);

        TextView add = chip("添加数据");
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(-2, -2);
        addParams.leftMargin = dp(10);
        add.setLayoutParams(addParams);
        add.setOnClickListener(v -> {
            MockData.addHealthData("heart_rate", "69", "正常");
            startActivity(new Intent(requireContext(), ChatDetailActivity.class).putExtra("chat_title", "健康档案新增完成"));
        });
        row.addView(add);

        TextView bt = chip("蓝牙导入");
        LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(-2, -2);
        btParams.leftMargin = dp(10);
        bt.setLayoutParams(btParams);
        bt.setOnClickListener(v -> startActivity(new Intent(requireContext(), BluetoothActivity.class)));
        row.addView(bt);

        hsv.addView(row);
        return hsv;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(18), dp(22), dp(18));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(12);
        card.setLayoutParams(params);
        return card;
    }

    private TextView title(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(sp(18));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(FontScaleHelper.textPrimary(requireContext()));
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(sp(15));
        view.setTextColor(FontScaleHelper.textSecondary(requireContext()));
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private TextView chip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(sp(15));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(FontScaleHelper.textPrimary(requireContext()));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(16), dp(10), dp(16), dp(10));
        return view;
    }

}
