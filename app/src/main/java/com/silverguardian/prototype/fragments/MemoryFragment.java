package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.MemoryRecord;

import java.util.ArrayList;
import java.util.List;

public class MemoryFragment extends Fragment {
    private final List<MemoryRecord> visible = new ArrayList<>();
    private MemoryAdapter adapter;
    private Spinner categorySpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));
        root.setPadding(16, 16, 16, 16);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView title = title("记忆与回忆");
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        categorySpinner = new Spinner(requireContext());
        header.addView(categorySpinner, new LinearLayout.LayoutParams(220, -2));

        Button add = button("新增记忆");
        header.addView(add, new LinearLayout.LayoutParams(-2, 56));

        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemoryAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        refreshCategories();
        add.setOnClickListener(v -> showAddDialog());
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refreshList(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        refreshList();
        return root;
    }

    private void refreshCategories() {
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(MockData.memoryCategories())));
    }

    private void refreshList() {
        visible.clear();
        String selected = categorySpinner.getSelectedItem() == null ? "全部" : categorySpinner.getSelectedItem().toString();
        for (MemoryRecord record : MockData.getMemories()) {
            if ("全部".equals(selected) || record.category.equals(selected)) visible.add(record);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(36, 8, 36, 0);
        EditText content = input("记录一件重要的事");
        Spinner category = new Spinner(requireContext());
        category.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"健康", "爱好", "家庭", "其他"}));
        form.addView(content);
        form.addView(category);
        new AlertDialog.Builder(requireContext())
            .setTitle("新增记忆")
            .setView(form)
            .setPositiveButton("保存", (d, w) -> {
                if (!content.getText().toString().trim().isEmpty()) {
                    MockData.addMemory(content.getText().toString().trim(), category.getSelectedItem().toString());
                    refreshCategories();
                    refreshList();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private TextView title(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(24);
        view.setTextColor(getResources().getColor(R.color.text_primary));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private Button button(String text) {
        Button button = new Button(requireContext());
        button.setText(text);
        button.setTextSize(18);
        return button;
    }

    private EditText input(String hint) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setTextSize(18);
        return editText;
    }

    private class MemoryAdapter extends RecyclerView.Adapter<MemoryHolder> {
        @NonNull @Override public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(24, 20, 24, 20);
            row.setBackgroundColor(getResources().getColor(R.color.surface_white));
            return new MemoryHolder(row);
        }
        @Override public void onBindViewHolder(@NonNull MemoryHolder holder, int position) { holder.bind(visible.get(position)); }
        @Override public int getItemCount() { return visible.size(); }
    }

    private class MemoryHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView meta;
        MemoryHolder(@NonNull View itemView) {
            super(itemView);
            content = new TextView(itemView.getContext());
            meta = new TextView(itemView.getContext());
            LinearLayout row = (LinearLayout) itemView;
            content.setTextSize(20);
            content.setTextColor(getResources().getColor(R.color.text_primary));
            meta.setTextSize(15);
            meta.setTextColor(getResources().getColor(R.color.text_secondary));
            row.addView(content);
            row.addView(meta);
        }
        void bind(MemoryRecord record) {
            content.setText(record.content);
            meta.setText(record.category + " · " + record.createdAt + " · 长按删除");
            itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("删除记忆")
                    .setMessage("确定删除这条记忆吗？")
                    .setPositiveButton("删除", (d, w) -> {
                        MockData.deleteMemory(record);
                        refreshList();
                    })
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
        }
    }
}
