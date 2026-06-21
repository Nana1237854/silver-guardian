package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.FraudApiClient;
import com.silverguardian.prototype.FraudDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

public class FraudFragment extends BaseFragment {
    private final List<FraudTip> visible = new ArrayList<>();
    private FraudAdapter adapter;
    private TextView loadingHint;
    private FraudApiClient apiClient;
    /** 标记当前展示的是否为远程数据，用于离线提示 */
    private boolean isRemoteData = false;

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
        isRemoteData = false;
        loadingHint.setText(getString(R.string.fraud_loaded_local, visible.size()));

        apiClient = new FraudApiClient();
        fetchRemote();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        maybeShowFraudGuide();
    }

    private void maybeShowFraudGuide() {
        if (!seniorGuide().shouldShow(com.silverguardian.prototype.modules.SeniorGuideModule.GUIDE_FRAUD)) {
            return;
        }
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.guide_fraud_title)
            .setMessage(R.string.guide_fraud_message)
            .setPositiveButton(R.string.common_ok, (dialog, which) ->
                seniorGuide().markShown(com.silverguardian.prototype.modules.SeniorGuideModule.GUIDE_FRAUD))
            .setCancelable(false)
            .show();
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

    private void fetchRemote() {
        loadingHint.setText(R.string.fraud_loading);
        apiClient.fetchFraudTips(new FraudApiClient.FraudCallback() {
            @Override
            public void onSuccess(List<FraudApiClient.FraudItem> items) {
                visible.clear();
                for (FraudApiClient.FraudItem item : items) {
                    visible.add(new FraudTip(
                        item.id, item.title, item.category, item.summary,
                        item.detail, item.risk, item.advice,
                        item.sourceName, item.sourceType, item.sourceDate));
                }
                isRemoteData = true;
                loadingHint.setText(getString(R.string.fraud_loaded_remote, items.size()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                if (visible.isEmpty()) {
                    visible.addAll(safetyContent().getFraudTips());
                    adapter.notifyDataSetChanged();
                }
                isRemoteData = false;
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
        private final TextView category;
        private final TextView summary;

        FraudHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fraud_item_title);
            category = itemView.findViewById(R.id.fraud_item_category);
            summary = itemView.findViewById(R.id.fraud_item_summary);
        }

        void bind(FraudTip tip) {
            title.setText(tip.title);
            // 分类标签 + 离线提示
            String catText = tip.category;
            if (!isRemoteData) {
                catText += " · " + getString(R.string.fraud_offline_badge);
            }
            category.setText(catText);
            // 显示摘要，为空时用 detail 截断兜底
            String sumText = !tip.summary.isEmpty() ? tip.summary
                : (tip.detail.length() > 60 ? tip.detail.substring(0, 60) + "…" : tip.detail);
            summary.setText(sumText);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), FraudDetailActivity.class);
                intent.putExtra("fraud_id", tip.id);
                intent.putExtra("fraud_title", tip.title);
                intent.putExtra("fraud_category", tip.category);
                intent.putExtra("fraud_summary", tip.summary);
                intent.putExtra("fraud_detail", tip.detail);
                intent.putExtra("fraud_risk", tip.risk);
                intent.putExtra("fraud_advice", tip.advice);
                intent.putExtra("fraud_source_name", tip.sourceName);
                intent.putExtra("fraud_source_type", tip.sourceType);
                intent.putExtra("fraud_source_date", tip.sourceDate);
                intent.putExtra("fraud_is_remote", isRemoteData);
                startActivity(intent);
            });
        }
    }
}
