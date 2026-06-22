package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.MemoryRecord;
import com.silverguardian.prototype.utils.FormFieldFactory;

import java.util.ArrayList;
import java.util.List;

// 记忆回忆页面：新增记忆、分类筛选、删除记忆
public class MemoryFragment extends BaseFragment {
    private final List<MemoryRecord> visible = new ArrayList<>();
    private MemoryAdapter adapter;
    private Spinner categorySpinner;
    private TextView emptyState;

    @Nullable
    @Override
    // 加载记忆页面布局、初始化筛选器与记忆列表
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_memory, container, false);
        bindHeader(root);
        bindFilter(root);
        bindList(root);
        seedMemoriesIfNeeded();
        refreshCategories();
        refreshList();
        return root;
    }

    private void bindHeader(View root) {
        View header = root.findViewById(R.id.memory_header);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);

        TextView title = header.findViewById(R.id.header_title);
        title.setText(R.string.memory_title);

        TextView action = header.findViewById(R.id.header_action);
        action.setVisibility(View.VISIBLE);
        action.setText(R.string.memory_add);
        action.setOnClickListener(v -> showAddDialog());
    }

    private void bindFilter(View root) {
        ((TextView) root.findViewById(R.id.memory_filter_title)).setText(R.string.memory_filter_title);
        ((TextView) root.findViewById(R.id.memory_filter_subtitle)).setText(R.string.memory_filter_subtitle);

        categorySpinner = root.findViewById(R.id.memory_category_spinner);
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                refreshList();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        emptyState = root.findViewById(R.id.memory_empty_state);
        emptyState.setText(R.string.memory_empty);
    }

    private void bindList(View root) {
        RecyclerView list = root.findViewById(R.id.memory_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemoryAdapter();
        list.setAdapter(adapter);
    }

    private void seedMemoriesIfNeeded() {
        if (!memories().getMemories().isEmpty()) {
            return;
        }
        String[] categories = getResources().getStringArray(R.array.memory_category_choices);
        memories().addMemory(getString(R.string.memory_seed_hobby), categories[1]);
        memories().addMemory(getString(R.string.memory_seed_family), categories[2]);
        memories().addMemory(getString(R.string.memory_seed_health), categories[0]);
    }

    private void refreshCategories() {
        ArrayList<String> categories = new ArrayList<>(memories().getCategories());
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories));
    }

    private void refreshList() {
        visible.clear();
        String selected = categorySpinner.getSelectedItem() == null ? getString(R.string.memory_all) : categorySpinner.getSelectedItem().toString();
        for (MemoryRecord record : memories().getMemories()) {
            if (com.silverguardian.prototype.modules.MemoryModule.ALL.equals(selected) || getString(R.string.memory_all).equals(selected) || record.category.equals(selected)) {
                visible.add(record);
            }
        }
        emptyState.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        LinearLayout form = (LinearLayout) LayoutInflater.from(requireContext())
            .inflate(R.layout.view_dialog_form_container, null, false);

        EditText content = FormFieldFactory.addTextField(requireContext(), form,
            getString(R.string.memory_content_label), getString(R.string.memory_content_hint), true);

        FormFieldFactory.addLabel(requireContext(), form, getString(R.string.memory_category_label));
        Spinner category = new Spinner(requireContext());
        category.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.memory_category_choices)));
        category.setPadding(0, dimen(R.dimen.action_row_padding_top), 0, 0);
        form.addView(category);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle(R.string.memory_add_dialog)
            .setView(form)
            .setPositiveButton(R.string.common_save, null)
            .setNegativeButton(R.string.common_cancel, null)
            .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = content.getText().toString().trim();
            if (text.isEmpty()) {
                content.setError(getString(R.string.memory_content_error));
                content.requestFocus();
                return;
            }
            memories().addMemory(text, category.getSelectedItem().toString());
            refreshCategories();
            refreshList();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private int dimen(int resId) {
        return getResources().getDimensionPixelSize(resId);
    }

    private class MemoryAdapter extends RecyclerView.Adapter<MemoryHolder> {
        @NonNull
        @Override
        public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_memory_record_item, parent, false);
            return new MemoryHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemoryHolder holder, int position) {
            holder.bind(visible.get(position));
        }

        @Override
        public int getItemCount() {
            return visible.size();
        }
    }

    private class MemoryHolder extends RecyclerView.ViewHolder {
        private final TextView content;
        private final TextView meta;

        MemoryHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.memory_item_content);
            meta = itemView.findViewById(R.id.memory_item_meta);
        }

        void bind(MemoryRecord record) {
            content.setText(record.content);
            meta.setText(getString(R.string.memory_meta_format, record.category, record.createdAt, getString(R.string.memory_delete_hint)));
            itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.memory_delete_title)
                    .setMessage(getString(R.string.memory_delete_confirm, record.content))
                    .setPositiveButton(R.string.memory_delete, (dialog, which) -> {
                        memories().deleteMemory(record);
                        refreshList();
                    })
                    .setNegativeButton(R.string.common_cancel, null)
                    .show();
                return true;
            });
        }
    }
}