package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.FraudApiClient;
import com.silverguardian.prototype.FraudDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

/**
 * 防诈骗知识推送 — 每日一推 + 历史列表。
 * OkHttp 拉取远程数据，网络异常时使用本地兜底。
 */
public class FraudFragment extends Fragment {
    private final List<FraudTip> visible = new ArrayList<>();
    private FraudAdapter adapter;
    private TextView loadingHint;
    private FraudApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));
        root.setPadding(dp(20), dp(20), dp(20), dp(20));

        // 标题栏
        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView title = new TextView(requireContext());
        title.setText("🛡️ 防诈骗知识推送");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView refreshBtn = chip("刷新");
        refreshBtn.setOnClickListener(v -> fetchRemote());
        header.addView(refreshBtn);
        root.addView(header);

        // 加载提示
        loadingHint = new TextView(requireContext());
        loadingHint.setText("正在从网络获取最新防诈骗知识...");
        loadingHint.setTextSize(14);
        loadingHint.setTextColor(getResources().getColor(R.color.text_secondary));
        loadingHint.setPadding(0, dp(12), 0, dp(12));
        root.addView(loadingHint);

        // 列表
        RecyclerView list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FraudAdapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        // 优先加载本地数据展示
        visible.addAll(MockData.getFraudTips());
        adapter.notifyDataSetChanged();
        loadingHint.setText("已加载 " + visible.size() + " 条防诈骗提示");

        // OkHttp 异步拉取远程数据
        apiClient = new FraudApiClient();
        fetchRemote();
        return root;
    }

    private void fetchRemote() {
        loadingHint.setText("正在从网络获取最新防诈骗知识...");
        apiClient.fetchFraudTips(new FraudApiClient.FraudCallback() {
            @Override
            public void onSuccess(List<FraudApiClient.FraudItem> items) {
                visible.clear();
                for (FraudApiClient.FraudItem item : items) {
                    visible.add(new FraudTip(visible.size() + 1, item.title, item.category, item.summary, item.detail + (item.measures.isEmpty() ? "" : "\n\n应对措施：" + item.measures)));
                }
                loadingHint.setText("✅ 已从网络获取 " + items.size() + " 条最新防诈骗知识");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                // 网络失败：回退到本地数据
                if (visible.isEmpty()) {
                    visible.addAll(MockData.getFraudTips());
                    adapter.notifyDataSetChanged();
                }
                loadingHint.setText("⚠️ 网络获取失败（" + error + "），使用本地缓存数据");
            }
        });
    }

    private class FraudAdapter extends RecyclerView.Adapter<FraudHolder> {
        @NonNull @Override
        public FraudHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(parent.getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(22), dp(20), dp(22), dp(20));
            row.setBackgroundResource(R.drawable.bg_card_surface);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(12);
            row.setLayoutParams(lp);
            return new FraudHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull FraudHolder holder, int position) { holder.bind(visible.get(position)); }
        @Override
        public int getItemCount() { return visible.size(); }
    }

    private class FraudHolder extends RecyclerView.ViewHolder {
        TextView title, meta;
        FraudHolder(@NonNull View itemView) {
            super(itemView);
            title = new TextView(itemView.getContext());
            meta = new TextView(itemView.getContext());
            LinearLayout row = (LinearLayout) itemView;
            title.setTextSize(20);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setTextColor(getResources().getColor(R.color.text_primary));
            meta.setTextSize(15);
            meta.setTextColor(getResources().getColor(R.color.text_secondary));
            meta.setPadding(0, dp(8), 0, 0);
            row.addView(title);
            row.addView(meta);
        }

        void bind(FraudTip tip) {
            title.setText(tip.title);
            meta.setText(tip.category + " · " + (tip.action != null ? tip.action : "点击查看详情"));
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

    private TextView chip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(14);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(14), dp(8), dp(14), dp(8));
        return view;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
