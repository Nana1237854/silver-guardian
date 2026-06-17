package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

public class FraudFragment extends Fragment {
    private final List<FraudTip> visible = new ArrayList<>();
    private FraudAdapter adapter;
    private Spinner categorySpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));
        root.setPadding(16, 16, 16, 16);

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView title = new TextView(requireContext());
        title.setText("防诈骗提示");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        categorySpinner = new Spinner(requireContext());
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(MockData.fraudCategories())));
        header.addView(categorySpinner, new LinearLayout.LayoutParams(260, -2));

        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FraudAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refreshList(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        refreshList();
        return root;
    }

    private void refreshList() {
        visible.clear();
        String selected = categorySpinner.getSelectedItem() == null ? "全部" : categorySpinner.getSelectedItem().toString();
        for (FraudTip tip : MockData.getFraudTips()) {
            if ("全部".equals(selected) || tip.category.equals(selected)) visible.add(tip);
        }
        adapter.notifyDataSetChanged();
    }

    private class FraudAdapter extends RecyclerView.Adapter<FraudHolder> {
        @NonNull @Override public FraudHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(24, 22, 24, 22);
            row.setBackgroundColor(getResources().getColor(R.color.surface_white));
            return new FraudHolder(row);
        }
        @Override public void onBindViewHolder(@NonNull FraudHolder holder, int position) { holder.bind(visible.get(position)); }
        @Override public int getItemCount() { return visible.size(); }
    }

    private class FraudHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;
        FraudHolder(@NonNull View itemView) {
            super(itemView);
            title = new TextView(itemView.getContext());
            meta = new TextView(itemView.getContext());
            LinearLayout row = (LinearLayout) itemView;
            title.setTextSize(21);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setTextColor(getResources().getColor(R.color.text_primary));
            meta.setTextSize(16);
            meta.setTextColor(getResources().getColor(R.color.text_secondary));
            row.addView(title);
            row.addView(meta);
        }
        void bind(FraudTip tip) {
            title.setText(tip.title);
            meta.setText(tip.category + " · " + tip.action);
            itemView.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(tip.title)
                .setMessage(tip.content + "\n\n应对方式：" + tip.action)
                .setPositiveButton("我知道了", null)
                .show());
        }
    }
}
