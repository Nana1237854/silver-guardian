package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

/**
 * 记忆回忆 — 生活记事 CRUD，支持分类筛选。
 */
public class MemoryFragment extends BaseFragment {
    private final List<MemoryRecord> visible = new ArrayList<>();
    private MemoryAdapter adapter;
    private Spinner categorySpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(getResources().getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(110));
        scroll.addView(root);

        // 标题栏
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        TextView title = new TextView(requireContext());
        title.setText("记忆回忆");
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        Button addBtn = btn("+ 新增", R.drawable.bg_button_primary, getResources().getColor(R.color.surface_white));
        addBtn.setOnClickListener(v -> showAddDialog());
        header.addView(addBtn);

        root.addView(header);

        // 分类筛选
        categorySpinner = new Spinner(requireContext());
        categorySpinner.setPadding(dp(16), dp(8), dp(16), dp(8));
        categorySpinner.setBackgroundResource(R.drawable.bg_chip_soft);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(220, dp(48));
        spinnerParams.bottomMargin = dp(16);
        categorySpinner.setLayoutParams(spinnerParams);
        root.addView(categorySpinner);

        // 列表
        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemoryAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        refreshCategories();
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refreshList(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 默认无记忆时给出引导
        if (MockData.getMemories().isEmpty()) {
            MockData.addMemory("喜欢每天傍晚去小区花园散步，看看花看看树。", "爱好");
            MockData.addMemory("孙子小明下个月就要上小学了，真快啊。", "家庭");
            MockData.addMemory("医生建议每天监测血压，早晚各一次。", "健康");
            refreshCategories();
        }
        refreshList();
        return scroll;
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
        form.setPadding(dp(32), dp(12), dp(32), 0);

        EditText content = new EditText(requireContext());
        content.setHint("记录一件重要的事...");
        content.setTextSize(18);
        content.setMinLines(3);
        content.setPadding(dp(16), dp(12), dp(16), dp(12));
        content.setBackgroundResource(R.drawable.bg_pin_input);
        form.addView(content);

        Spinner category = new Spinner(requireContext());
        category.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"健康", "爱好", "家庭", "其他"}));
        category.setPadding(0, dp(12), 0, 0);
        form.addView(category);

        new AlertDialog.Builder(requireContext())
            .setTitle("新增记忆")
            .setView(form)
            .setPositiveButton("保存", (d, w) -> {
                String text = content.getText().toString().trim();
                if (!text.isEmpty()) {
                    MockData.addMemory(text, category.getSelectedItem().toString());
                    refreshCategories();
                    refreshList();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private Button btn(String text, int bgRes, int textColor) {
        Button btn = new Button(requireContext());
        btn.setText(text);
        btn.setTextSize(16);
        btn.setAllCaps(false);
        btn.setBackgroundResource(bgRes);
        btn.setTextColor(textColor);
        btn.setPadding(dp(18), dp(8), dp(18), dp(8));
        return btn;
    }

    private class MemoryAdapter extends RecyclerView.Adapter<MemoryHolder> {
        @NonNull @Override
        public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(20), dp(18), dp(20), dp(18));
            row.setBackgroundResource(R.drawable.bg_group_surface);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(12);
            row.setLayoutParams(lp);
            return new MemoryHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull MemoryHolder holder, int position) { holder.bind(visible.get(position)); }
        @Override
        public int getItemCount() { return visible.size(); }
    }

    private class MemoryHolder extends RecyclerView.ViewHolder {
        TextView content, meta;
        MemoryHolder(@NonNull View itemView) {
            super(itemView);
            content = new TextView(itemView.getContext());
            meta = new TextView(itemView.getContext());
            LinearLayout row = (LinearLayout) itemView;
            content.setTextSize(20);
            content.setTextColor(getResources().getColor(R.color.text_primary));
            content.setLineSpacing(dp(2), 1f);
            meta.setTextSize(14);
            meta.setTextColor(getResources().getColor(R.color.text_secondary));
            meta.setPadding(0, dp(10), 0, 0);
            row.addView(content);
            row.addView(meta);
        }

        void bind(MemoryRecord record) {
            content.setText(record.content);
            meta.setText(record.category + " · " + record.createdAt + " · 长按删除");
            itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("删除记忆")
                    .setMessage("确定删除这条记忆吗？\n\n" + record.content)
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
