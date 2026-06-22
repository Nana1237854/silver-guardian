package com.silverguardian.prototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.utils.FormFieldFactory;

import java.util.ArrayList;
import java.util.List;

public class FamilyManageActivity extends BaseActivity {
    private final List<FamilyMember> familyMembers = new ArrayList<>();

    private RecyclerView listView;
    private TextView emptyView;
    private FamilyMemberAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_manage);
        bindHeader();
        bindList();
        bindActions();
        refreshMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybeShowFamilyGuide();
    }

    private void maybeShowFamilyGuide() {
        if (!seniorGuide().shouldShow(com.silverguardian.prototype.modules.SeniorGuideModule.GUIDE_FAMILY_MANAGE)) {
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle(R.string.guide_family_title)
            .setMessage(R.string.guide_family_message)
            .setPositiveButton(R.string.common_ok, (dialog, which) ->
                seniorGuide().markShown(com.silverguardian.prototype.modules.SeniorGuideModule.GUIDE_FAMILY_MANAGE))
            .setCancelable(false)
            .show();
    }

    private void bindHeader() {
        View header = findViewById(R.id.family_manage_header);
        TextView back = header.findViewById(R.id.header_back);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.family_manage_title);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindList() {
        listView = findViewById(R.id.family_manage_list);
        emptyView = findViewById(R.id.family_manage_empty);
        adapter = new FamilyMemberAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
    }

    private void bindActions() {
        TextView addButton = findViewById(R.id.family_manage_add_button);
        addButton.setText(R.string.family_manage_add_button);
        addButton.setOnClickListener(v -> showFamilyDialog(null));
    }

    private void refreshMembers() {
        familyMembers.clear();
        familyMembers.addAll(userSession().getFamilyMembers());
        adapter.notifyDataSetChanged();
        boolean isEmpty = familyMembers.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showFamilyDialog(@Nullable FamilyMember member) {
        boolean editing = member != null;
        LinearLayout form = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.view_dialog_form_container, null, false);
        EditText nameInput = FormFieldFactory.addTextField(this, form,
            getString(R.string.family_manage_name_label), getString(R.string.family_manage_name_hint), false);
        EditText relationshipInput = FormFieldFactory.addTextField(this, form,
            getString(R.string.family_manage_relationship_label), getString(R.string.family_manage_relationship_hint), false);
        EditText phoneInput = FormFieldFactory.addTextField(this, form,
            getString(R.string.family_manage_phone_label), getString(R.string.family_manage_phone_hint), false);

        if (editing) {
            nameInput.setText(member.name);
            relationshipInput.setText(member.relationship);
            phoneInput.setText(member.phone);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(editing ? R.string.family_manage_edit_title : R.string.family_manage_add_title)
            .setView(form)
            .setPositiveButton(R.string.common_save, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String relationship = relationshipInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            if (!validate(nameInput, relationshipInput, phoneInput, name, relationship, phone)) {
                return;
            }
            if (editing) {
                userSession().updateFamilyMember(member, name, relationship, phone);
            } else {
                userSession().addFamilyMember(name, relationship, phone);
            }
            refreshMembers();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private boolean validate(EditText nameInput, EditText relationshipInput, EditText phoneInput,
                             String name, String relationship, String phone) {
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.family_manage_name_required));
            nameInput.requestFocus();
            return false;
        }
        if (relationship.isEmpty()) {
            relationshipInput.setError(getString(R.string.family_manage_relationship_required));
            relationshipInput.requestFocus();
            return false;
        }
        if (phone.isEmpty()) {
            phoneInput.setError(getString(R.string.family_manage_phone_required));
            phoneInput.requestFocus();
            return false;
        }
        if (phone.replace(" ", "").length() < 6) {
            phoneInput.setError(getString(R.string.family_manage_phone_too_short));
            phoneInput.requestFocus();
            return false;
        }
        return true;
    }

    private void confirmDelete(FamilyMember member) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.family_manage_delete_title)
            .setMessage(getString(R.string.family_manage_delete_message, member.name))
            .setPositiveButton(R.string.common_delete, (dialog, which) -> {
                userSession().deleteFamilyMember(member);
                refreshMembers();
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberHolder> {
        @NonNull
        @Override
        public FamilyMemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_member, parent, false);
            return new FamilyMemberHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull FamilyMemberHolder holder, int position) {
            holder.bind(familyMembers.get(position));
        }

        @Override
        public int getItemCount() {
            return familyMembers.size();
        }
    }

    private class FamilyMemberHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView relationshipView;
        private final TextView phoneView;
        private final View onlineDot;

        FamilyMemberHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.family_name);
            relationshipView = itemView.findViewById(R.id.family_relationship);
            phoneView = itemView.findViewById(R.id.family_phone);
            onlineDot = itemView.findViewById(R.id.family_dot);
        }

        void bind(FamilyMember member) {
            nameView.setText(member.name);
            relationshipView.setText(member.relationship);
            phoneView.setText(member.phone);
            onlineDot.setVisibility(member.online ? View.VISIBLE : View.INVISIBLE);
            itemView.setOnClickListener(v -> showFamilyDialog(member));
            itemView.setOnLongClickListener(v -> {
                confirmDelete(member);
                return true;
            });
        }
    }
}
