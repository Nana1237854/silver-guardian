package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页中枢 — 健康快报 + 家属列表 + AI 对话入口。
 */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(getResources().getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(110));
        scroll.addView(root);

        // 日期 + 天气
        root.addView(dateWeatherRow());

        // AI 对话入口卡片
        root.addView(aiEntryCard());

        // 健康快报
        root.addView(sectionHeader("❤️ 健康快报"));
        root.addView(healthSnapshotCard());

        // 家属列表
        root.addView(sectionHeader("👨‍👩‍👧‍👦 家人"));
        root.addView(familyListCard());

        // 快捷操作
        root.addView(sectionHeader("快捷操作"));
        root.addView(quickActions());

        return scroll;
    }

    private View dateWeatherRow() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(16));

        TextView date = new TextView(requireContext());
        date.setText(new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE).format(new Date()));
        date.setTextSize(20);
        date.setTypeface(null, android.graphics.Typeface.BOLD);
        date.setTextColor(getResources().getColor(R.color.text_primary));
        row.addView(date, new LinearLayout.LayoutParams(0, -2, 1));

        TextView weather = chip("☀️ 晴 26°C");
        row.addView(weather);
        return row;
    }

    private View aiEntryCard() {
        LinearLayout card = card();
        card.setBackgroundResource(R.drawable.bg_card_accent);
        card.setGravity(Gravity.CENTER);

        TextView icon = new TextView(requireContext());
        icon.setText("🤖");
        icon.setTextSize(40);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, 0, 0, dp(8));
        card.addView(icon);

        TextView title = new TextView(requireContext());
        title.setText("智能守护助手");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setGravity(Gravity.CENTER);
        card.addView(title);

        TextView hint = new TextView(requireContext());
        hint.setText("点击开始 AI 对话 · 健康咨询 · 药品推荐");
        hint.setTextSize(15);
        hint.setTextColor(getResources().getColor(R.color.text_secondary));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(6), 0, dp(4));
        card.addView(hint);

        card.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
        return card;
    }

    private View healthSnapshotCard() {
        LinearLayout card = card();

        List<HealthData> data = MockData.getHealthData();
        String[][] priority = {{"heart_rate", "心率"}, {"blood_pressure", "血压"}, {"blood_sugar", "血糖"}, {"steps", "步数"}, {"temperature", "体温"}};
        int shown = 0;
        for (String[] key : priority) {
            for (HealthData h : data) {
                if (h.type.equals(key[0]) && shown < 5) {
                    card.addView(metricRow(key[1], h.value + " " + h.getUnit(), h.status));
                    shown++;
                    break;
                }
            }
        }
        if (shown == 0) {
            TextView empty = body("暂无健康数据，去健康探索添加吧");
            card.addView(empty);
        }

        // 查看详情
        TextView detail = chip("查看详情 →");
        detail.setGravity(Gravity.CENTER);
        detail.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).openHealth();
        });
        card.addView(detail);
        return card;
    }

    private View metricRow(String label, String value, String status) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        TextView labelView = body(label);
        labelView.setTextSize(15);
        row.addView(labelView, new LinearLayout.LayoutParams(dp(72), -2));

        TextView valueView = new TextView(requireContext());
        valueView.setText(value);
        valueView.setTextSize(15);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        valueView.setTextColor(getResources().getColor(R.color.text_primary));
        row.addView(valueView, new LinearLayout.LayoutParams(0, -2, 1));

        TextView statusView = miniChip(status);
        row.addView(statusView);
        return row;
    }

    private View familyListCard() {
        LinearLayout card = card();

        List<FamilyMember> members = MockData.getFamilyMembers();
        if (members.isEmpty()) {
            card.addView(body("暂无家属信息"));
            return card;
        }

        for (FamilyMember m : members) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            View dot = new View(requireContext());
            dot.setLayoutParams(new LinearLayout.LayoutParams(dp(10), dp(10)));
            dot.setBackgroundResource(m.online ? R.drawable.dot_online : R.drawable.dot_offline);
            row.addView(dot);

            TextView nameView = new TextView(requireContext());
            nameView.setText(m.name + "（" + m.relationship + "）");
            nameView.setTextSize(16);
            nameView.setTextColor(getResources().getColor(R.color.text_primary));
            nameView.setPadding(dp(10), 0, 0, 0);
            row.addView(nameView, new LinearLayout.LayoutParams(0, -2, 1));

            if (m.phone != null && !m.phone.isEmpty()) {
                TextView phone = miniChip(m.phone);
                row.addView(phone);
            }
            card.addView(row);
        }
        return card;
    }

    private View quickActions() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(quickActionBtn("📞 一键呼叫", v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).oneTapCall();
        }));
        row.addView(quickActionBtn("🩺 健康档案", v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).openHealth();
        }));
        row.addView(quickActionBtn("💊 用药提醒", v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).findViewById(R.id.bottom_navigation).callOnClick();
            // Navigate to medicine via MainActivity
            com.google.android.material.bottomnavigation.BottomNavigationView nav =
                getActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) nav.setSelectedItemId(R.id.bottom_medicine);
        }));

        return row;
    }

    private View quickActionBtn(String text, View.OnClickListener listener) {
        LinearLayout btn = new LinearLayout(requireContext());
        btn.setOrientation(LinearLayout.VERTICAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(12), dp(14), dp(12), dp(14));
        btn.setBackgroundResource(R.drawable.bg_chip_soft);
        btn.setOnClickListener(listener);

        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(getResources().getColor(R.color.text_primary));
        tv.setGravity(Gravity.CENTER);
        btn.addView(tv);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.rightMargin = dp(8);
        btn.setLayoutParams(params);
        return btn;
    }

    // ========== Utils ==========

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(14);
        card.setLayoutParams(params);
        return card;
    }

    private TextView sectionHeader(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(18);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.text_primary));
        view.setPadding(0, dp(4), 0, dp(8));
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(getResources().getColor(R.color.text_secondary));
        return view;
    }

    private TextView chip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(14);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(14), dp(8), dp(14), dp(8));
        return view;
    }

    private TextView miniChip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(12);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(8), dp(4), dp(8), dp(4));
        return view;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
