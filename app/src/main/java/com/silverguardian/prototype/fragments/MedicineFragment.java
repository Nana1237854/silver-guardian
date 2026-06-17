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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.MedicineLibraryItem;
import com.silverguardian.prototype.utils.FontScaleHelper;

import java.util.ArrayList;
import java.util.List;

public class MedicineFragment extends BaseFragment {
    private MedicineAdapter adapter;
    private final List<Medicine> visible = new ArrayList<>();
    private Spinner typeFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine, container, false);
        RecyclerView list = view.findViewById(R.id.medicine_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineAdapter();
        list.setAdapter(adapter);

        typeFilter = view.findViewById(R.id.medicine_filter);
        refreshFilter();
        typeFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refresh(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        view.findViewById(R.id.add_medicine_fab).setOnClickListener(v -> showAddMedicineDialog());

        // 验收用：长按 FAB 触发一次 TTS 语音播报测试
        view.findViewById(R.id.add_medicine_fab).setOnLongClickListener(v -> {
            android.content.Intent i = new android.content.Intent(requireContext(),
                com.silverguardian.prototype.reminder.ReminderBroadcastReceiver.class);
            i.putExtra("medicine_name", "硝苯地平缓释片");
            i.putExtra("user_name", "颜爷爷");
            requireContext().sendBroadcast(i);
            Toast.makeText(requireContext(), "已发送测试用药提醒（通知+TTS语音）", Toast.LENGTH_LONG).show();
            return true;
        });

        refresh();
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        if (adapter != null) refresh();
    }

    private void refresh() {
        visible.clear();
        String selected = typeFilter == null || typeFilter.getSelectedItem() == null ? "全部类型" : typeFilter.getSelectedItem().toString();
        for (Medicine medicine : MockData.getMedicines()) {
            if ("全部类型".equals(selected) || medicine.type.equals(selected)) visible.add(medicine);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void refreshFilter() {
        typeFilter.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(MockData.medicineTypes())));
    }

    private void showAddMedicineDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(36, 8, 36, 0);

        EditText search = input("药品库搜索：高血压 / 感冒 / 药名");
        EditText name = input("药品名称");
        EditText type = input("类型，例如：降压药");
        EditText time = input("服用时间，例如：08:00,20:00");
        EditText method = input("用法，例如：口服");
        EditText desc = input("说明");
        form.addView(search);
        form.addView(name);
        form.addView(type);
        form.addView(time);
        form.addView(method);
        form.addView(desc);

        search.setOnEditorActionListener((v, actionId, event) -> {
            List<MedicineLibraryItem> result = MockData.searchMedicineLibrary(search.getText().toString());
            if (!result.isEmpty()) {
                MedicineLibraryItem item = result.get(0);
                name.setText(item.name);
                type.setText(item.type);
                desc.setText(item.brand + " · " + item.description);
                time.setText("08:00");
                method.setText("口服");
                Toast.makeText(getContext(), "已填入药品库首个匹配项", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        new AlertDialog.Builder(requireContext())
            .setTitle("添加药品提醒")
            .setView(form)
            .setPositiveButton("保存", (dialog, which) -> {
                String medName = name.getText().toString().trim();
                if (medName.isEmpty()) {
                    Toast.makeText(getContext(), "请填写药品名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                MockData.addMedicine(
                    medName,
                    type.getText().toString().trim().isEmpty() ? "其他" : type.getText().toString().trim(),
                    time.getText().toString().trim().isEmpty() ? "08:00" : time.getText().toString().trim(),
                    method.getText().toString().trim().isEmpty() ? "口服" : method.getText().toString().trim(),
                    desc.getText().toString().trim()
                );
                refreshFilter();
                refresh();
            })
            .setNeutralButton("查看药品库", (dialog, which) -> showLibraryDialog(""))
            .setNegativeButton("取消", null)
            .show();
    }

    private void showLibraryDialog(String keyword) {
        List<MedicineLibraryItem> items = MockData.searchMedicineLibrary(keyword);
        String[] names = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            MedicineLibraryItem item = items.get(i);
            names[i] = item.disease + " → " + item.name + "（" + item.brand + "）";
        }
        new AlertDialog.Builder(requireContext())
            .setTitle("药品库")
            .setItems(names, (d, which) -> {
                MedicineLibraryItem item = items.get(which);
                MockData.addMedicine(item.name, item.type, "08:00", "口服", item.brand + " · " + item.description);
                refreshFilter();
                refresh();
            })
            .setPositiveButton("关闭", null)
            .show();
    }

    private EditText input(String hint) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setTextSize(sp(18));
        editText.setSingleLine(false);
        return editText;
    }

    // sp() inherited from BaseFragment

    private class MedicineAdapter extends RecyclerView.Adapter<MedicineViewHolder> {
        @NonNull @Override public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
            return new MedicineViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull MedicineViewHolder h, int pos) { h.bind(visible.get(pos)); }
        @Override public int getItemCount() { return visible.size(); }
    }

    private class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView name, time, method;
        CheckBox checkbox;

        MedicineViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.med_name);
            time = v.findViewById(R.id.med_time);
            method = v.findViewById(R.id.med_method);
            checkbox = v.findViewById(R.id.med_check);
        }

        void bind(Medicine medicine) {
            name.setText(medicine.name);
            time.setText("⏰ " + medicine.time.replace(",", " · "));
            method.setText(medicine.type + " · " + medicine.method + " · " + medicine.description);
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(medicine.takenToday);
            checkbox.setText(medicine.takenToday ? "已打卡" : "未打卡");
            checkbox.setOnCheckedChangeListener((btn, checked) -> {
                MockData.toggleMedicineTaken(medicine, checked);
                checkbox.setText(checked ? "已打卡" : "未打卡");
                Toast.makeText(getContext(), checked ? "已完成服药打卡" : "已取消打卡", Toast.LENGTH_SHORT).show();
            });
            itemView.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(medicine.name)
                .setMessage("类型：" + medicine.type + "\n时间：" + medicine.time + "\n用法：" + medicine.method + "\n说明：" + medicine.description)
                .setPositiveButton("知道了", null)
                .setNeutralButton("删除", (d, w) -> confirmDelete(medicine))
                .show());
            itemView.setOnLongClickListener(v -> {
                confirmDelete(medicine);
                return true;
            });
        }
    }

    private void confirmDelete(Medicine medicine) {
        new AlertDialog.Builder(requireContext())
            .setTitle("删除药品")
            .setMessage("确定删除「" + medicine.name + "」吗？")
            .setPositiveButton("删除", (d, w) -> {
                MockData.deleteMedicine(medicine);
                refresh();
            })
            .setNegativeButton("取消", null)
            .show();
    }
}
