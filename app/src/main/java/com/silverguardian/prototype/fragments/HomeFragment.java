package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.R;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(110));
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));

        TextView title = text("首页中枢", 28, true);
        root.addView(title);

        TextView assistant = text("银发守护助手", 24, true);
        assistant.setGravity(android.view.Gravity.CENTER);
        assistant.setBackgroundResource(R.drawable.bg_card_surface);
        assistant.setPadding(dp(20), dp(28), dp(20), dp(28));
        assistant.setOnClickListener(v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(18);
        root.addView(assistant, params);

        TextView hint = text("点击进入完整 AI 对话。健康探索、用药提醒、亲情相册与设置已在底部导航中完成迁移。", 16, false);
        hint.setTextColor(getResources().getColor(R.color.text_secondary));
        hint.setPadding(0, dp(16), 0, 0);
        root.addView(hint);
        return root;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getResources().getColor(R.color.text_primary));
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
