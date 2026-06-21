package com.silverguardian.prototype.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.BluetoothActivity;
import com.silverguardian.prototype.ChildModeActivity;
import com.silverguardian.prototype.CareSummaryActivity;
import com.silverguardian.prototype.CommunityActivity;
import com.silverguardian.prototype.FamilyManageActivity;
import com.silverguardian.prototype.LoginActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.reminder.FraudNotificationHelper;
import com.silverguardian.prototype.reminder.FraudReminderScheduler;
import com.silverguardian.prototype.utils.FontScaleHelper;

public class SettingsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        bindHeader(root);
        bindProfileCard(root);
        bindSections(root);
        return root;
    }

    private void bindHeader(View root) {
        View header = root.findViewById(R.id.page_header_root);
        if (header == null) {
            return;
        }
        header.findViewById(R.id.header_back).setVisibility(View.GONE);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.settings_title);
    }

    private void bindProfileCard(View root) {
        View card = root.findViewById(R.id.settings_profile_card);
        if (card == null) {
            return;
        }

        ((ImageView) card.findViewById(R.id.settings_avatar)).setImageResource(R.drawable.elder_profile);
        ((ImageView) card.findViewById(R.id.settings_avatar)).setContentDescription(getString(R.string.settings_profile_name));
        ((TextView) card.findViewById(R.id.settings_name)).setText(R.string.settings_profile_name);
        ((TextView) card.findViewById(R.id.settings_badge)).setText(R.string.settings_profile_badge);
        ((TextView) card.findViewById(R.id.settings_detail)).setText(R.string.settings_profile_detail);
        ((TextView) card.findViewById(R.id.settings_arrow)).setText(R.string.settings_arrow);
        card.setOnClickListener(v -> openSimpleDialog(R.string.settings_profile_name, R.string.settings_profile_detail));
    }

    private void bindSections(View root) {
        LinearLayout familyGroup = bindGroupSection(root, R.id.settings_family_section, R.string.settings_section_family_devices);
        LinearLayout safetyGroup = bindGroupSection(root, R.id.settings_safety_section, R.string.settings_section_safety_memory);
        LinearLayout displayGroup = bindGroupSection(root, R.id.settings_display_section, R.string.settings_section_display_accessibility);
        LinearLayout otherGroup = bindGroupSection(root, R.id.settings_other_section, R.string.settings_section_other);

        addActionRow(familyGroup, R.drawable.ic_family, R.drawable.bg_icon_mint, color(R.color.primary),
            R.string.settings_child_mode_title, R.string.settings_child_mode_desc,
            v -> startActivity(new Intent(requireContext(), ChildModeActivity.class)), true);
        addDivider(familyGroup);
        addActionRow(familyGroup, R.drawable.ic_info, R.drawable.bg_icon_lilac, color(R.color.memory),
            R.string.settings_manage_family_title, R.string.settings_manage_family_desc,
            v -> startActivity(new Intent(requireContext(), FamilyManageActivity.class)), false);
        addDivider(familyGroup);
        addActionRow(familyGroup, R.drawable.ic_info, R.drawable.bg_icon_blue, color(R.color.info),
            R.string.care_summary_entry_title, R.string.care_summary_entry_desc,
            v -> startActivity(new Intent(requireContext(), CareSummaryActivity.class)), false);
        addDivider(familyGroup);
        addActionRow(familyGroup, R.drawable.ic_bluetooth, R.drawable.bg_icon_blue, color(R.color.info),
            R.string.settings_bluetooth_title, R.string.settings_bluetooth_desc,
            v -> startActivity(new Intent(requireContext(), BluetoothActivity.class)), false);
        addDivider(familyGroup);
        addActionRow(familyGroup, R.drawable.ic_community, R.drawable.bg_icon_coral, color(R.color.accent_orange),
            R.string.settings_community_title, R.string.settings_community_desc,
            v -> startActivity(new Intent(requireContext(), CommunityActivity.class)), false);

        addActionRow(safetyGroup, R.drawable.ic_shield, R.drawable.bg_icon_coral, color(R.color.accent_orange),
            R.string.settings_fraud_title, R.string.settings_fraud_desc, v -> switchToFragment("fraud"), false);
        addDivider(safetyGroup);
        // 濮ｅ繑妫╅梼鑼剁槗閹绘劙鍟嬪鈧崗?
        addPreferenceRow(safetyGroup, R.drawable.ic_notifications, R.drawable.bg_icon_blue, color(R.color.info),
            R.string.fraud_daily_reminder_title, R.string.fraud_daily_reminder_desc, buildFraudReminderControl());
        addDivider(safetyGroup);
        addActionRow(safetyGroup, R.drawable.ic_memory, R.drawable.bg_icon_lilac, color(R.color.memory),
            R.string.settings_memory_title, R.string.settings_memory_desc, v -> switchToFragment("memory"), false);
        addDivider(safetyGroup);
        addActionRow(safetyGroup, R.drawable.ic_lock, R.drawable.bg_icon_mint, color(R.color.primary),
            R.string.settings_data_permission_title, R.string.settings_data_permission_desc,
            v -> openSimpleDialog(R.string.settings_data_permission_title, R.string.settings_data_dialog_body), false);

        addPreferenceRow(displayGroup, R.drawable.ic_text_size, R.drawable.bg_icon_lilac, color(R.color.memory),
            R.string.settings_font_size_title, R.string.settings_font_size_desc, buildFontSizeControl());
        addDivider(displayGroup);
        addPreferenceRow(displayGroup, R.drawable.ic_contrast, R.drawable.bg_icon_mint, color(R.color.primary),
            R.string.settings_high_contrast_title, R.string.settings_high_contrast_desc, buildHighContrastControl());
        addDivider(displayGroup);
        addActionRow(displayGroup, R.drawable.ic_notifications, R.drawable.bg_icon_blue, color(R.color.info),
            R.string.settings_notifications_title, R.string.settings_notifications_desc,
            v -> openSimpleDialog(R.string.settings_notifications_title, R.string.settings_notifications_desc), false);

        addActionRow(otherGroup, R.drawable.ic_info, R.drawable.bg_icon_mint, color(R.color.primary),
            R.string.settings_about_title, R.string.settings_about_desc,
            v -> openSimpleDialog(R.string.settings_about_title, R.string.settings_about_dialog_body), false);
        addDivider(otherGroup);
        addActionRow(otherGroup, R.drawable.ic_logout, R.drawable.bg_icon_coral, color(R.color.sos_red),
            R.string.settings_logout_title, R.string.settings_logout_desc, v -> logout(), false);
    }

    private LinearLayout bindGroupSection(View root, int sectionId, int titleRes) {
        View section = root.findViewById(sectionId);
        TextView title = section.findViewById(R.id.section_group_title);
        title.setText(titleRes);
        return section.findViewById(R.id.section_group_content);
    }

    private void addActionRow(LinearLayout group, int iconRes, int iconBg, int tint, int titleRes,
                              int subtitleRes, View.OnClickListener listener, boolean familyPreview) {
        View row = inflateRow();
        row.setOnClickListener(listener);

        ImageView icon = row.findViewById(R.id.settings_row_icon);
        TextView rowTitle = row.findViewById(R.id.settings_row_title);
        TextView rowSubtitle = row.findViewById(R.id.settings_row_subtitle);
        FrameLayout end = row.findViewById(R.id.settings_row_end_container);

        icon.setImageResource(iconRes);
        icon.setBackgroundResource(iconBg);
        icon.setColorFilter(tint);
        icon.setContentDescription(getString(titleRes));
        rowTitle.setText(titleRes);
        rowSubtitle.setText(subtitleRes);

        if (familyPreview) {
            ImageView family = new ImageView(requireContext());
            family.setImageResource(R.drawable.family_companion);
            family.setScaleType(ImageView.ScaleType.CENTER_CROP);
            family.setContentDescription(getString(R.string.settings_child_mode_title));
            end.addView(family, new FrameLayout.LayoutParams(dimen(R.dimen.settings_family_preview_width), dimen(R.dimen.settings_family_preview_height)));
        } else {
            View arrow = LayoutInflater.from(requireContext()).inflate(R.layout.view_settings_row_arrow, end, false);
            end.addView(arrow);
        }

        group.addView(row);
    }

    private void addPreferenceRow(LinearLayout group, int iconRes, int iconBg, int tint, int titleRes,
                                  int subtitleRes, View control) {
        View row = inflateRow();

        ImageView icon = row.findViewById(R.id.settings_row_icon);
        TextView rowTitle = row.findViewById(R.id.settings_row_title);
        TextView rowSubtitle = row.findViewById(R.id.settings_row_subtitle);
        FrameLayout end = row.findViewById(R.id.settings_row_end_container);

        icon.setImageResource(iconRes);
        icon.setBackgroundResource(iconBg);
        icon.setColorFilter(tint);
        icon.setContentDescription(getString(titleRes));
        rowTitle.setText(titleRes);
        rowSubtitle.setText(subtitleRes);
        end.addView(control);

        group.addView(row);
    }

    private Spinner buildFontSizeControl() {
        Spinner spinner = new Spinner(requireContext());
        String[] fontModes = {
            getString(R.string.settings_font_standard),
            getString(R.string.settings_font_large),
            getString(R.string.settings_font_extra_large)
        };
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fontModes));
        spinner.setContentDescription(getString(R.string.settings_font_size_title));
        spinner.setSelection(FontScaleHelper.getFontModeIndex(requireContext()));
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position != FontScaleHelper.getFontModeIndex(requireContext())) {
                    FontScaleHelper.setFontMode(requireContext(), position);
                    requireActivity().recreate();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private TextView buildHighContrastControl() {
        TextView toggle = (TextView) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_settings_row_status, null, false);
        boolean enabled = FontScaleHelper.isHighContrast(requireContext());
        toggle.setText(enabled ? R.string.settings_toggle_on : R.string.settings_toggle_off);
        toggle.setContentDescription(getString(R.string.settings_toggle_content_desc, getString(R.string.settings_high_contrast_title), toggle.getText()));
        toggle.setSelected(enabled);
        toggle.setOnClickListener(v -> {
            FontScaleHelper.setHighContrast(requireContext(), !FontScaleHelper.isHighContrast(requireContext()));
            requireActivity().recreate();
        });
        return toggle;
    }

    private TextView buildFraudReminderControl() {
        TextView toggle = (TextView) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_settings_row_status, null, false);
        boolean enabled = FraudReminderScheduler.isReminderEnabled(requireContext());
        toggle.setText(enabled ? R.string.settings_toggle_on : R.string.settings_toggle_off);
        toggle.setContentDescription(getString(R.string.settings_toggle_content_desc, getString(R.string.fraud_daily_reminder_title), toggle.getText()));
        toggle.setSelected(enabled);
        toggle.setOnClickListener(v -> {
            boolean next = !FraudReminderScheduler.isReminderEnabled(requireContext());
            if (next) {
                // Android 13+ 闂団偓鐟曚線鈧氨鐓￠弶鍐
                if (Build.VERSION.SDK_INT >= 33) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                        // 閺夊啴妾洪幒鍫滅埃閸氬骸鍟€瀵偓閸氼垽绱濈憴?onRequestPermissionsResult
                        return;
                    }
                }
                enableReminder();
            } else {
                disableReminder();
            }
        });
        return toggle;
    }

    private void enableReminder() {
        FraudReminderScheduler.setReminderEnabled(requireContext(), true);
        FraudNotificationHelper.createChannel(requireContext());
        FraudReminderScheduler.scheduleDailyReminder(requireContext());
        requireActivity().recreate();
    }

    private void disableReminder() {
        FraudReminderScheduler.setReminderEnabled(requireContext(), false);
        FraudReminderScheduler.cancelDailyReminder(requireContext());
        requireActivity().recreate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableReminder();
            } else {
                toast(getString(R.string.fraud_notification_permission_message));
            }
        }
    }

    private View inflateRow() {
        return LayoutInflater.from(requireContext()).inflate(R.layout.view_settings_row, null, false);
    }

    private void addDivider(LinearLayout group) {
        View divider = new View(requireContext());
        divider.setBackgroundColor(color(R.color.divider));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dimen(R.dimen.divider_height));
        params.leftMargin = dimen(R.dimen.settings_divider_inset_start);
        params.rightMargin = dimen(R.dimen.settings_divider_inset_end);
        group.addView(divider, params);
    }

    private int dimen(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    private void switchToFragment(String name) {
        MainActivity main = (MainActivity) requireActivity();
        if ("fraud".equals(name)) {
            main.switchToFraud();
        }
        if ("memory".equals(name)) {
            main.switchToMemory();
        }
    }

    private void openSimpleDialog(int titleRes, int messageRes) {
        new AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(R.string.common_ok, null)
            .show();
    }

    private void logout() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}


