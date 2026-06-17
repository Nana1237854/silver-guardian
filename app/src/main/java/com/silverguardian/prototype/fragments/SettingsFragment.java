package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.BluetoothActivity;
import com.silverguardian.prototype.ChildModeActivity;
import com.silverguardian.prototype.LoginActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.utils.FontScaleHelper;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setBackgroundColor(getResources().getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(110));
        scrollView.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = titleView("设置");
        title.setTextSize(28);
        root.addView(title);

        root.addView(profileCard());
        root.addView(fontSizeCard());
        root.addView(highContrastCard());
        root.addView(featureCard("子女模式", "查看家人健康状况与提醒", "👪", v -> startActivity(new Intent(requireContext(), ChildModeActivity.class))));
        root.addView(featureCard("蓝牙设备", "连接血压计、血氧仪等", "⌁", v -> startActivity(new Intent(requireContext(), BluetoothActivity.class))));
        root.addView(featureCard("便民查询", "附近菜市场/医院/药店搜索与导航", "📍", v -> startActivity(new Intent(requireContext(), com.silverguardian.prototype.CommunityActivity.class))));
        root.addView(featureCard("防诈提醒", "每日推送防电信诈骗知识", "盾", v -> switchToFragment("fraud")));
        root.addView(featureCard("记忆回忆", "生活记事与回忆珍藏", "▣", v -> switchToFragment("memory")));
        root.addView(featureCard("健康数据授权", "管理数据分享与隐私权限", "锁", v -> openSimpleDialog("健康数据授权", "本原生版本默认所有数据仅保存在本机 SQLite。")));
        root.addView(featureCard("退出登录", "返回老人档案选择页面", "↩", v -> {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        }));
        return scrollView;
    }

    private View profileCard() {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        TextView avatar = titleView("颜");
        avatar.setTextSize(30);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.bg_circle_gray);
        card.addView(avatar, new LinearLayout.LayoutParams(dp(72), dp(72)));

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(18), 0, 0, 0);
        info.addView(titleView("颜爷爷"));
        info.addView(body("已守护 128 天\n72 岁 · 男性"));
        card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        return card;
    }

    private View fontSizeCard() {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(titleView("大字模式"));
        texts.addView(body("调整全局字体大小，方便阅读"));
        card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        Spinner spinner = new Spinner(requireContext());
        String[] modes = {"标准", "加大", "特大"};
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modes));
        spinner.setSelection(FontScaleHelper.getFontModeIndex(requireContext()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int current = FontScaleHelper.getFontModeIndex(requireContext());
                if (position != current) {
                    FontScaleHelper.setFontMode(requireContext(), position);
                    requireActivity().recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        card.addView(spinner, new LinearLayout.LayoutParams(dp(150), -2));
        return card;
    }

    private View highContrastCard() {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(titleView("高对比度"));
        texts.addView(body("增强文字与背景的对比度"));

        card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        TextView toggle = chip(FontScaleHelper.isHighContrast(requireContext()) ? "✓ 已开启" : "○ 已关闭");
        toggle.setOnClickListener(v -> {
            boolean current = FontScaleHelper.isHighContrast(requireContext());
            FontScaleHelper.setHighContrast(requireContext(), !current);
            requireActivity().recreate();
        });
        card.addView(toggle);
        return card;
    }

    private View featureCard(String title, String subtitle, String mark, View.OnClickListener listener) {
        LinearLayout card = card();
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setOnClickListener(listener);

        TextView icon = titleView(mark);
        icon.setTextSize(22);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(R.drawable.bg_chip_soft);
        card.addView(icon, new LinearLayout.LayoutParams(dp(56), dp(56)));

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(16), 0, 0, 0);
        texts.addView(titleView(title));
        texts.addView(body(subtitle));
        card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = body("›");
        arrow.setTextSize(28);
        card.addView(arrow);
        return card;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(16);
        card.setLayoutParams(params);
        return card;
    }

    private TextView titleView(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(20);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.text_primary));
        return view;
    }

    private TextView body(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(14);
        view.setTextColor(getResources().getColor(R.color.text_secondary));
        view.setPadding(0, dp(4), 0, 0);
        return view;
    }

    private TextView chip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(15);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(16), dp(10), dp(16), dp(10));
        return view;
    }

    private void switchToFragment(String name) {
        if (!(getActivity() instanceof MainActivity)) return;
        MainActivity main = (MainActivity) getActivity();
        if ("fraud".equals(name)) {
            main.switchToFraud();
        } else if ("memory".equals(name)) {
            main.switchToMemory();
        }
    }

    private void openSimpleDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("知道了", null)
            .show();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
