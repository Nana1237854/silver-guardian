package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.FraudApiClient;
import com.silverguardian.prototype.FraudDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

// 防诈提醒列表页：远程刷新防诈数据、本地兜底展示
public class FraudFragment extends BaseFragment {
    private final List<FraudTip> visible = new ArrayList<>();
    private FraudAdapter adapter;
    private TextView loadingHint;
    private FraudApiClient apiClient;

    // 初始化视图、加载本地防诈内容并尝试远程获取
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fraud, container, false);
        bindHeader(root);
        bindControls(root);
        bindList(root);

        visible.clear();
        visible.addAll(safetyContent().getFraudTips());
        adapter.notifyDataSetChanged();
        loadingHint.setText(getString(R.string.fraud_loaded_local, visible.size()));

        apiClient = new FraudApiClient();
        fetchRemote();
        return root;
    }

    private void bindHeader(View root) {
        View header = root.findViewById(R.id.fraud_header);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.fraud_title);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindControls(View root) {
        loadingHint = root.findViewById(R.id.fraud_loading_hint);
        loadingHint.setText(R.string.fraud_loading);
        loadingHint.setBackgroundResource(R.drawable.bg_card_surface);
        loadingHint.setTextColor(color(R.color.text_secondary));

        TextView refresh = root.findViewById(R.id.fraud_refresh);
        refresh.setOnClickListener(v -> fetchRemote());
    }

    private void bindList(View root) {
        RecyclerView list = root.findViewById(R.id.fraud_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FraudAdapter();
        list.setAdapter(adapter);
    }

    // 从远程API拉取防诈数据并更新列表
    private void fetchRemote() {
        loadingHint.setText(R.string.fraud_loading);
        apiClient.fetchFraudTips(new FraudApiClient.FraudCallback() {
            @Override
            public void onSuccess(List<FraudApiClient.FraudItem> items) {
                visible.clear();
                for (FraudApiClient.FraudItem item : items) {
                    String content = item.detail + (item.measures.isEmpty() ? "" : getString(R.string.fraud_measures_prefix) + item.measures);
                    visible.add(new FraudTip(visible.size() + 1, item.title, item.category, item.summary, content));
                }
                loadingHint.setText(getString(R.string.fraud_loaded_remote, items.size()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                if (visible.isEmpty()) {
                    visible.addAll(safetyContent().getFraudTips());
                    adapter.notifyDataSetChanged();
                }
                loadingHint.setText(getString(R.string.fraud_network_fail, error));
            }
        });
    }

    private class FraudAdapter extends RecyclerView.Adapter<FraudHolder> {
        @NonNull
        @Override
        public FraudHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fraud_tip, parent, false);
            return new FraudHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FraudHolder holder, int position) {
            holder.bind(visible.get(position));
        }

        @Override
        public int getItemCount() {
            return visible.size();
        }
    }

    private class FraudHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;

        FraudHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fraud_item_title);
            meta = itemView.findViewById(R.id.fraud_item_meta);
        }

        void bind(FraudTip tip) {
            title.setText(tip.title);
            meta.setText(tip.category + " · " + (tip.action != null ? tip.action : getString(R.string.fraud_action_view_detail)));
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), FraudDetailActivity.class);
                intent.putExtra("fraud_title", tip.title);
                intent.putExtra("fraud_category", tip.category);
                intent.putExtra("fraud_detail", tip.content);
                intent.putExtra("fraud_measures", tip.action);
                startActivity(intent);
            });
        }
    }
}