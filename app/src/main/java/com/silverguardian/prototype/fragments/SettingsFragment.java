package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.silverguardian.prototype.BluetoothActivity;
import com.silverguardian.prototype.ChildModeActivity;
import com.silverguardian.prototype.CommunityActivity;
import com.silverguardian.prototype.LoginActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.utils.FontScaleHelper;

public class SettingsFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(color(R.color.bg_page));
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(112));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        root.addView(text("设置", 28, true));
        root.addView(profileCard());

        root.addView(section("家人与设备"));
        root.addView(group(
            featureRow(R.drawable.ic_health, R.drawable.bg_icon_mint, color(R.color.primary),
                "子女模式", "查看家人健康状况与提醒", v -> startActivity(new Intent(requireContext(), ChildModeActivity.class)), true),
            featureRow(R.drawable.ic_upload, R.drawable.bg_icon_blue, 0xFF3188C8,
                "蓝牙设备", "连接血压计、血氧仪等设备", v -> startActivity(new Intent(requireContext(), BluetoothActivity.class)), false),
            featureRow(R.drawable.ic_home, R.drawable.bg_icon_coral, color(R.color.accent_orange),
                "便民查询", "查找附近医院、药店与菜市场", v -> startActivity(new Intent(requireContext(), CommunityActivity.class)), false)
        ));

        root.addView(section("安全与回忆"));
        root.addView(group(
            featureRow(R.drawable.ic_health, R.drawable.bg_icon_coral, color(R.color.accent_orange),
                "防诈提醒", "学习防诈知识，守护财产安全", v -> switchToFragment("fraud"), false),
            featureRow(R.drawable.ic_album, R.drawable.bg_icon_lilac, 0xFF6F61D9,
                "记忆回忆", "生活记事与回忆珍藏", v -> switchToFragment("memory"), false),
            featureRow(R.drawable.ic_logout, R.drawable.bg_icon_mint, color(R.color.primary),
                "健康数据授权", "管理数据分享与隐私权限", v -> openSimpleDialog("健康数据授权", "当前所有健康数据仅保存在本机 SQLite，未经允许不会分享。"), false)
        ));

        root.addView(section("显示与辅助"));
        root.addView(group(fontSizeRow(), highContrastRow(),
            featureRow(R.drawable.ic_health, R.drawable.bg_icon_blue, 0xFF3188C8,
                "通知设置", "管理用药、健康与家人通知", v -> openSimpleDialog("通知设置", "用药提醒、健康异常与家人消息通知均已开启。"), false)
        ));

        root.addView(section("其他"));
        root.addView(group(
            featureRow(R.drawable.ic_home, R.drawable.bg_icon_mint, color(R.color.primary),
                "关于我们", "银发守护者 · 智能健康陪伴", v -> openSimpleDialog("关于银发守护者", "专为长辈设计的智能陪伴与健康守护应用。"), false),
            featureRow(R.drawable.ic_logout, R.drawable.bg_icon_coral, color(R.color.sos_red),
                "退出登录", "返回老人档案选择页面", v -> logout(), false)
        ));
        return scroll;
    }

    private View profileCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_profile_card);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(16);
        card.setLayoutParams(cp);

        ImageView avatar = new ImageView(requireContext());
        avatar.setImageResource(R.drawable.elder_profile);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setContentDescription("颜爷爷头像");
        card.addView(avatar, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(15), 0, 0, 0);
        info.addView(text("颜爷爷", 21, true));
        TextView guarded = text("已守护 128 天", 13, true);
        guarded.setTextColor(color(R.color.status_good));
        guarded.setBackgroundResource(R.drawable.bg_status_good);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-2, -2);
        gp.topMargin = dp(5);
        info.addView(guarded, gp);
        TextView detail = text("72 岁 · 男性", 14, false);
        detail.setTextColor(color(R.color.text_secondary));
        detail.setPadding(0, dp(6), 0, 0);
        info.addView(detail);
        card.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 30, false);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(color(R.color.text_secondary));
        card.addView(arrow, new LinearLayout.LayoutParams(dp(48), dp(48)));
        return card;
    }

    private TextView section(String title) {
        TextView view = text(title, 16, true);
        view.setTextColor(color(R.color.text_secondary));
        view.setPadding(dp(4), dp(22), 0, dp(8));
        return view;
    }

    private View group(View... rows) {
        LinearLayout group = new LinearLayout(requireContext());
        group.setOrientation(LinearLayout.VERTICAL);
        group.setBackgroundResource(R.drawable.bg_group_surface);
        for (int i = 0; i < rows.length; i++) {
            group.addView(rows[i]);
            if (i < rows.length - 1) {
                View divider = new View(requireContext());
                divider.setBackgroundColor(color(R.color.divider));
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, 1);
                dp.leftMargin = this.dp(74);
                dp.rightMargin = this.dp(16);
                group.addView(divider, dp);
            }
        }
        return group;
    }

    private View featureRow(int iconRes, int iconBg, int tint, String title, String subtitle,
                            View.OnClickListener listener, boolean familyPreview) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(86));
        row.setPadding(dp(14), dp(10), dp(10), dp(10));
        row.setBackgroundResource(android.R.drawable.list_selector_background);
        row.setOnClickListener(listener);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(tint);
        icon.setBackgroundResource(iconBg);
        icon.setPadding(dp(13), dp(13), dp(13), dp(13));
        icon.setContentDescription(title);
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout copy = new LinearLayout(requireContext());
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(13), 0, dp(6), 0);
        copy.addView(text(title, 18, true));
        TextView sub = text(subtitle, 13, false);
        sub.setTextColor(color(R.color.text_secondary));
        sub.setPadding(0, dp(4), 0, 0);
        copy.addView(sub);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        if (familyPreview) {
            ImageView family = new ImageView(requireContext());
            family.setImageResource(R.drawable.family_companion);
            family.setScaleType(ImageView.ScaleType.CENTER_CROP);
            family.setContentDescription("家属陪伴插画");
            row.addView(family, new LinearLayout.LayoutParams(dp(76), dp(58)));
        } else {
            TextView arrow = text("›", 28, false);
            arrow.setTextColor(color(R.color.text_secondary));
            arrow.setGravity(Gravity.CENTER);
            row.addView(arrow, new LinearLayout.LayoutParams(dp(44), dp(48)));
        }
        return row;
    }

    private View fontSizeRow() {
        LinearLayout row = preferenceRow(R.drawable.ic_album, R.drawable.bg_icon_lilac,
            0xFF6F61D9, "字体大小", "调整全局字号，方便阅读");
        Spinner spinner = new Spinner(requireContext());
        String[] modes = {"标准", "加大", "特大"};
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, modes));
        spinner.setSelection(FontScaleHelper.getFontModeIndex(requireContext()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != FontScaleHelper.getFontModeIndex(requireContext())) {
                    FontScaleHelper.setFontMode(requireContext(), position);
                    requireActivity().recreate();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        row.addView(spinner, new LinearLayout.LayoutParams(dp(116), dp(52)));
        return row;
    }

    private View highContrastRow() {
        LinearLayout row = preferenceRow(R.drawable.ic_health, R.drawable.bg_icon_mint,
            color(R.color.primary), "高对比度", "增强文字、图标和边界对比");
        TextView toggle = text(FontScaleHelper.isHighContrast(requireContext()) ? "已开启" : "已关闭", 13, true);
        toggle.setTextColor(color(R.color.primary_dark));
        toggle.setBackgroundResource(R.drawable.bg_status_good);
        toggle.setGravity(Gravity.CENTER);
        toggle.setOnClickListener(v -> {
            FontScaleHelper.setHighContrast(requireContext(), !FontScaleHelper.isHighContrast(requireContext()));
            requireActivity().recreate();
        });
        row.addView(toggle, new LinearLayout.LayoutParams(dp(84), dp(48)));
        return row;
    }

    private LinearLayout preferenceRow(int iconRes, int iconBg, int tint, String title, String subtitle) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(86));
        row.setPadding(dp(14), dp(10), dp(10), dp(10));
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(tint);
        icon.setBackgroundResource(iconBg);
        icon.setPadding(dp(13), dp(13), dp(13), dp(13));
        icon.setContentDescription(title);
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout copy = new LinearLayout(requireContext());
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(13), 0, dp(6), 0);
        copy.addView(text(title, 18, true));
        TextView sub = text(subtitle, 13, false);
        sub.setTextColor(color(R.color.text_secondary));
        sub.setPadding(0, dp(4), 0, 0);
        copy.addView(sub);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(sp(size));
        view.setTextColor(FontScaleHelper.textPrimary(requireContext()));
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private void switchToFragment(String name) {
        MainActivity main = (MainActivity) requireActivity();
        if ("fraud".equals(name)) main.switchToFraud();
        if ("memory".equals(name)) main.switchToMemory();
    }

    private void openSimpleDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
            .setTitle(title).setMessage(message).setPositiveButton("知道了", null).show();
    }

    private void logout() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}
