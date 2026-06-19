package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.EmergencyAlert;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.utils.FormFieldFactory;

import java.util.List;

public class ChildModeActivity extends BaseActivity {
    private static final int PICK_IMAGE = 601;
    private static final int REQUEST_READ_IMAGES = 602;

    private Uri pendingImageUri;
    private String pendingTitle;
    private String pendingCategory;
    private String pendingMessage;

    private LinearLayout healthSection;
    private LinearLayout medicineSection;
    private LinearLayout alertSection;
    private LinearLayout familySection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        setContentView(R.layout.activity_child_mode);
        initDraftValues();
        bindHeader();
        bindSections();
        bindActions();
        refreshContent();
    }

    private void initDraftValues() {
        pendingTitle = getString(R.string.child_mode_default_title);
        pendingCategory = getString(R.string.child_mode_default_category);
        pendingMessage = getString(R.string.child_mode_default_message);
    }

    private void bindHeader() {
        View header = findViewById(R.id.child_mode_header);
        TextView back = header.findViewById(R.id.header_back);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.tab_child);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindSections() {
        healthSection = bindCardSection(R.id.child_mode_health_block, R.string.child_mode_section_health);
        medicineSection = bindCardSection(R.id.child_mode_medicine_block, R.string.child_mode_section_medicine);
        alertSection = bindCardSection(R.id.child_mode_alert_block, R.string.child_mode_section_alert);
        familySection = bindCardSection(R.id.child_mode_family_block, R.string.child_mode_section_family);
    }

    private LinearLayout bindCardSection(int blockId, int titleRes) {
        View block = findViewById(blockId);
        ((TextView) block.findViewById(R.id.section_card_title)).setText(titleRes);
        return block.findViewById(R.id.section_card_content);
    }

    private void bindActions() {
        ((TextView) findViewById(R.id.child_mode_upload_button)).setText(R.string.child_mode_upload_for_elder);
        ((TextView) findViewById(R.id.child_mode_back_button)).setText(R.string.child_mode_back_to_elder);
        findViewById(R.id.child_mode_upload_button).setOnClickListener(v -> showUploadDialog());
        findViewById(R.id.child_mode_back_button).setOnClickListener(v -> finish());
    }

    private void refreshContent() {
        bindHealthSummary();
        bindMedicineSummary();
        bindAlerts();
        bindFamily();
    }

    private void bindHealthSummary() {
        healthSection.removeAllViews();
        List<HealthData> data = MockData.getHealthData();
        String[][] keys = {
            {"heart_rate", getString(R.string.health_metric_heart_rate)},
            {"steps", getString(R.string.health_metric_steps)},
            {"blood_pressure", getString(R.string.health_metric_blood_pressure)},
            {"blood_sugar", getString(R.string.health_metric_blood_sugar)},
            {"sleep", getString(R.string.health_metric_sleep)},
            {"mood", getString(R.string.health_metric_mood)}
        };
        int count = 0;
        for (String[] key : keys) {
            for (HealthData h : data) {
                if (h.type.equals(key[0])) {
                    addDetailRow(healthSection, key[1], h.value + " " + h.getUnit(), h.status, true);
                    count++;
                    break;
                }
            }
        }
        if (count == 0) {
            addEmptyState(healthSection, R.string.child_mode_empty_health);
        }
    }

    private void bindMedicineSummary() {
        medicineSection.removeAllViews();
        List<Medicine> meds = MockData.getMedicines();
        if (meds.isEmpty()) {
            addEmptyState(medicineSection, R.string.child_mode_empty_medicine);
            return;
        }
        int taken = 0;
        for (Medicine medicine : meds) {
            if (medicine.takenToday) {
                taken++;
            }
        }

        TextView summary = (TextView) LayoutInflater.from(this).inflate(R.layout.view_status_strip, medicineSection, false);
        summary.setText(getString(R.string.child_mode_medicine_progress, taken, meds.size()));
        summary.setTextColor(getColor(taken == meds.size() ? R.color.primary_dark : R.color.accent_orange_dark));
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(-1, -2);
        summaryParams.bottomMargin = dimen(R.dimen.child_intro_gap_top);
        medicineSection.addView(summary, summaryParams);

        for (Medicine medicine : meds) {
            addDetailRow(
                medicineSection,
                medicine.name,
                medicine.time.replace(",", "  -  ") + "  |  " + medicine.method,
                medicine.takenToday ? getString(R.string.child_mode_medicine_done) : getString(R.string.child_mode_medicine_undone),
                false
            );
        }
    }

    private void bindAlerts() {
        alertSection.removeAllViews();
        List<EmergencyAlert> alerts = MockData.getEmergencyAlerts();
        if (alerts.isEmpty()) {
            addEmptyState(alertSection, R.string.child_mode_empty_alert);
            return;
        }
        int show = Math.min(alerts.size(), 3);
        for (int i = 0; i < show; i++) {
            EmergencyAlert alert = alerts.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_child_mode_alert_row, alertSection, false);
            ((TextView) row.findViewById(R.id.child_alert_time)).setText(alert.time);
            ((TextView) row.findViewById(R.id.child_alert_message)).setText(alert.message);
            ((TextView) row.findViewById(R.id.child_alert_status)).setText(alert.status);
            alertSection.addView(row);
        }
    }

    private void bindFamily() {
        familySection.removeAllViews();
        List<FamilyMember> members = MockData.getFamilyMembers();
        if (members.isEmpty()) {
            addEmptyState(familySection, R.string.child_mode_empty_family);
            return;
        }
        for (FamilyMember member : members) {
            String title = getString(R.string.child_mode_family_title, member.name, member.relationship);
            String subtitle = member.phone == null ? "" : member.phone;
            addDetailRow(familySection, title, subtitle, member.online ? getString(R.string.family_online) : getString(R.string.family_offline), true);
        }
    }

    private void addDetailRow(LinearLayout parent, String title, String subtitle, String chip, boolean subduedChip) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_child_mode_detail_row, parent, false);
        ((TextView) row.findViewById(R.id.child_detail_title)).setText(title);
        TextView subtitleView = row.findViewById(R.id.child_detail_subtitle);
        if (subtitle == null || subtitle.trim().isEmpty()) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(subtitle);
        }
        TextView chipView = row.findViewById(R.id.child_detail_chip);
        chipView.setText(chip);
        chipView.setTextColor(getColor(subduedChip ? R.color.primary_dark : R.color.accent_orange_dark));
        parent.addView(row);
    }

    private void addEmptyState(LinearLayout parent, int textRes) {
        TextView empty = (TextView) LayoutInflater.from(this).inflate(R.layout.view_empty_state, parent, false);
        empty.setText(textRes);
        parent.addView(empty);
    }

    private void showUploadDialog() {
        pendingImageUri = null;
        View form = LayoutInflater.from(this).inflate(R.layout.view_child_mode_upload_form, null, false);
        TextView pickButton = form.findViewById(R.id.child_mode_pick_button);
        TextView selectedState = form.findViewById(R.id.child_mode_selected_state);
        LinearLayout fieldsContainer = form.findViewById(R.id.child_mode_upload_fields_container);

        EditText titleInput = FormFieldFactory.addTextField(this, fieldsContainer,
            getString(R.string.child_mode_photo_title_label), getString(R.string.child_mode_photo_title_hint), false);
        EditText categoryInput = FormFieldFactory.addTextField(this, fieldsContainer,
            getString(R.string.child_mode_category_label), getString(R.string.child_mode_category_hint), false);
        EditText messageInput = FormFieldFactory.addTextField(this, fieldsContainer,
            getString(R.string.child_mode_message_label), getString(R.string.child_mode_message_hint), true);
        titleInput.setText(pendingTitle);
        categoryInput.setText(pendingCategory);
        messageInput.setText(pendingMessage);

        pickButton.setOnClickListener(v -> {
            pendingTitle = titleInput.getText().toString().trim().isEmpty() ? pendingTitle : titleInput.getText().toString().trim();
            pendingCategory = categoryInput.getText().toString().trim().isEmpty() ? pendingCategory : categoryInput.getText().toString().trim();
            pendingMessage = messageInput.getText().toString().trim().isEmpty() ? pendingMessage : messageInput.getText().toString().trim();
            requestGalleryPermissionAndOpen();
            selectedState.setText(pendingImageUri == null ? getString(R.string.child_mode_photo_pending) : getString(R.string.child_mode_photo_selected));
        });

        new AlertDialog.Builder(this)
            .setTitle(R.string.upload_photo)
            .setView(form)
            .setPositiveButton(R.string.common_save, (dialog, which) -> {
                pendingTitle = titleInput.getText().toString().trim().isEmpty() ? pendingTitle : titleInput.getText().toString().trim();
                pendingCategory = categoryInput.getText().toString().trim().isEmpty() ? pendingCategory : categoryInput.getText().toString().trim();
                pendingMessage = messageInput.getText().toString().trim().isEmpty() ? pendingMessage : messageInput.getText().toString().trim();
                MockData.addPhoto(pendingTitle, pendingCategory, pendingMessage);
                Toast.makeText(this, R.string.child_mode_upload_success, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void requestGalleryPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_IMAGES);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_IMAGES);
                return;
            }
        }
        openGallery();
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            pendingImageUri = data.getData();
            Toast.makeText(this, R.string.child_mode_photo_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, R.string.child_mode_album_permission_toast, Toast.LENGTH_LONG).show();
            }
        }
    }

    private int dimen(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }
}