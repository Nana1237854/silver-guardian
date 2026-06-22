package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.utils.FormFieldFactory;

import java.util.ArrayList;
import java.util.List;

// 用药提醒页面：药品列表、添加药品、今日打卡、筛选
public class MedicineFragment extends BaseFragment {
    private MedicineAdapter adapter;
    private final List<Medicine> visible = new ArrayList<>();
    private Spinner typeFilter;
    private TextView progressView;
    private TextView emptyState;

    @Nullable
    @Override
    // 创建用药提醒页面视图：绑定列表、控件、刷新数据
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine, container, false);
        bindList(view);
        refresh();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            refresh();
        }
    }

    private void bindHeader(View root) {
        View header = root.findViewById(R.id.medicine_header);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.medicine_title);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindControls(View root) {
        TextView addButton = root.findViewById(R.id.add_medicine_button);
        addButton.setText(R.string.medicine_add);
        addButton.setOnClickListener(v -> showAddMedicineDialog());
        addButton.setOnLongClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(),
                com.silverguardian.prototype.reminder.ReminderBroadcastReceiver.class);
            intent.putExtra("medicine_name", getString(R.string.medicine_demo_name));
            intent.putExtra("user_name", getString(R.string.settings_profile_name));
            requireContext().sendBroadcast(intent);
            Toast.makeText(requireContext(), R.string.medicine_test_broadcast, Toast.LENGTH_LONG).show();
            return true;
        });

        progressView = root.findViewById(R.id.medicine_progress);
        emptyState = root.findViewById(R.id.medicine_empty_state);
        emptyState.setText(R.string.medicine_empty);

        TextView reminder = root.findViewById(R.id.medicine_reminder_strip);
        reminder.setText(R.string.medicine_reminder);

        typeFilter = root.findViewById(R.id.medicine_filter);
        typeFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void bindList(View root) {
        RecyclerView list = root.findViewById(R.id.medicine_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineAdapter();
        list.setAdapter(adapter);
    }

    private void refresh() {
        visible.clear();
        String selected = typeFilter == null || typeFilter.getSelectedItem() == null ? getString(R.string.medicine_all_types) : typeFilter.getSelectedItem().toString();
        for (Medicine medicine : medicineReminders().getMedicines()) {
            if (com.silverguardian.prototype.modules.MedicineReminderModule.ALL_TYPES.equals(selected) || getString(R.string.medicine_all_types).equals(selected) || medicine.type.equals(selected)) {
                visible.add(medicine);
            }
        }
        int total = medicineReminders().getMedicines().size();
        int taken = 0;
        for (Medicine medicine : medicineReminders().getMedicines()) {
            if (medicine.takenToday) {
                taken++;
            }
        }
        if (progressView != null) {
            progressView.setText(getString(R.string.medicine_progress_prefix) + taken + "/" + total);
        }
        if (emptyState != null) {
            emptyState.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void refreshFilter() {
        typeFilter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(medicineReminders().getMedicineTypes())));
    }

    private void showAddMedicineDialog() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);

        EditText search = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_search_label), getString(R.string.medicine_search_hint), false);
        EditText name = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_name_label), getString(R.string.medicine_name_hint), false);
        EditText type = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_type_label), getString(R.string.medicine_type_hint), false);
        EditText time = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_time_label), getString(R.string.medicine_time_hint), false);
        EditText method = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_method_label), getString(R.string.medicine_method_hint), false);
        EditText desc = FormFieldFactory.addTextField(requireContext(), form, getString(R.string.medicine_desc_label), getString(R.string.medicine_desc_hint), true);
        Spinner advance = new Spinner(requireContext()); advance.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"准时提醒","提前 5 分钟","提前 10 分钟","提前 15 分钟"})); form.addView(advance);
        Spinner repeats = new Spinner(requireContext()); repeats.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"不重复","重复 1 次","重复 2 次","重复 3 次","重复 4 次","重复 5 次"})); form.addView(repeats);
        Spinner interval = new Spinner(requireContext()); interval.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"5 分钟","10 分钟","15 分钟","30 分钟"})); form.addView(interval);

        search.setOnEditorActionListener((v, actionId, event) -> {
            List<MedicineLibraryItem> result = medicineReminders().searchMedicineLibrary(search.getText().toString());
            if (!result.isEmpty()) {
                MedicineLibraryItem item = result.get(0);
                name.setText(item.name);
                type.setText(item.type);
                desc.setText(item.brand + " - " + item.description);
                time.setText("08:00");
                method.setText(R.string.medicine_oral);
                Toast.makeText(getContext(), R.string.medicine_fill_first_match, Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(R.string.medicine_add_dialog)
            .setView(form)
            .setPositiveButton(R.string.common_save, null)
            .setNeutralButton(R.string.medicine_library, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String medName = name.getText().toString().trim();
                if (medName.isEmpty()) {
                    name.setError(getString(R.string.medicine_name_error));
                    name.requestFocus();
                    return;
                }
                medicineReminders().addMedicine(
                    medName,
                    type.getText().toString().trim().isEmpty() ? getString(R.string.medicine_other) : type.getText().toString().trim(),
                    time.getText().toString().trim().isEmpty() ? "08:00" : time.getText().toString().trim(),
                    method.getText().toString().trim().isEmpty() ? getString(R.string.medicine_oral) : method.getText().toString().trim(),
                    desc.getText().toString().trim()
                );
                refreshFilter();
                refresh();
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> startActivity(new android.content.Intent(requireContext(), com.silverguardian.prototype.MedicineLibraryActivity.class).putExtra("editable", false)));
        });
        dialog.show();
    }

    private void showLibraryDialog(String keyword) {
        List<MedicineLibraryItem> items = medicineReminders().searchMedicineLibrary(keyword);
        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            MedicineLibraryItem item = items.get(i);
            names[i] = item.disease + " -> " + item.name + "(" + item.brand + ")";
        }
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.medicine_library_title)
            .setItems(names, (dialog, which) -> {
                MedicineLibraryItem item = items.get(which);
                medicineReminders().addMedicine(item.name, item.type, "08:00", getString(R.string.medicine_oral), item.brand + " - " + item.description);
                refreshFilter();
                refresh();
            })
            .setPositiveButton(R.string.common_close, null)
            .show();
    }

    private class MedicineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_MEDICINE = 1;

        @Override public int getItemViewType(int position) { return position == 0 ? TYPE_HEADER : TYPE_MEDICINE; }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                viewType == TYPE_HEADER ? R.layout.item_medicine_page_header : R.layout.item_medicine, parent, false);
            return viewType == TYPE_HEADER ? new MedicineHeaderHolder(view) : new MedicineViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MedicineHeaderHolder) ((MedicineHeaderHolder) holder).bind();
            else ((MedicineViewHolder) holder).bind(visible.get(position - 1));
        }

        @Override public int getItemCount() { return 1 + visible.size(); }
    }

    private class MedicineHeaderHolder extends RecyclerView.ViewHolder {
        final TextView progress;
        final TextView empty;
        final Spinner filter;

        MedicineHeaderHolder(View view) {
            super(view);
            View header = view.findViewById(R.id.medicine_header);
            header.findViewById(R.id.header_back).setVisibility(View.GONE);
            ((TextView) header.findViewById(R.id.header_title)).setText(R.string.medicine_title);
            header.findViewById(R.id.header_action).setVisibility(View.GONE);
            TextView add = view.findViewById(R.id.add_medicine_button);
            add.setText(R.string.medicine_add);
            add.setOnClickListener(v -> showAddMedicineDialog());
            progress = view.findViewById(R.id.medicine_progress);
            empty = view.findViewById(R.id.medicine_empty_state);
            empty.setText(R.string.medicine_empty);
            TextView reminder = view.findViewById(R.id.medicine_reminder_strip);
            reminder.setText(R.string.medicine_reminder);
            filter = view.findViewById(R.id.medicine_filter);
            filter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>(medicineReminders().getMedicineTypes())));
            filter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(android.widget.AdapterView<?> parent, View selected, int position, long id) { typeFilter = filter; refresh(); }
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
            });
            typeFilter = filter;
            progressView = progress;
            emptyState = empty;
        }

        void bind() {
            int total = medicineReminders().getMedicines().size();
            progress.setText(getString(R.string.medicine_progress_prefix) + medicineReminders().getTakenCount() + "/" + total);
            empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private class MedicineViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView time;
        private final TextView method;
        private final TextView type;
        private final CheckBox checkbox;

        MedicineViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.med_name);
            time = view.findViewById(R.id.med_time);
            method = view.findViewById(R.id.med_method);
            type = view.findViewById(R.id.med_type);
            checkbox = view.findViewById(R.id.med_check);
        }

        void bind(Medicine medicine) {
            name.setText(medicine.name);
            type.setText(medicine.type);
            time.setText(medicine.time.replace(",", "  -  "));
            method.setText(medicine.method + " - " + medicine.description);
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(medicine.takenToday);
            checkbox.setText(medicine.takenToday ? R.string.medicine_taken : R.string.medicine_untaken);
            checkbox.setOnCheckedChangeListener((buttonView, checked) -> {
                medicineReminders().toggleMedicineTaken(medicine, checked);
                checkbox.setText(checked ? R.string.medicine_taken : R.string.medicine_untaken);
                refresh();
                Toast.makeText(getContext(), checked ? R.string.medicine_done_toast : R.string.medicine_undone_toast, Toast.LENGTH_SHORT).show();
            });
            itemView.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(medicine.name)
                .setMessage(getString(R.string.medicine_detail_message, medicine.type, medicine.time, medicine.method, medicine.description))
                .setPositiveButton(R.string.medicine_detail_ok, null)
                .setNeutralButton(R.string.common_delete, (dialog, which) -> confirmDelete(medicine))
                .show());
            itemView.setOnLongClickListener(v -> { confirmDelete(medicine); return true; });
        }
    }
    private void confirmDelete(Medicine medicine) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.medicine_delete_title)
            .setMessage(getString(R.string.medicine_delete_confirm, medicine.name))
            .setPositiveButton(R.string.common_delete, (dialog, which) -> {
                medicineReminders().deleteMedicine(medicine);
                refresh();
            })
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }
}