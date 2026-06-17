package com.silverguardian.prototype;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.ChatMessage;

import java.util.List;

/**
 * AI 对话消息适配器 — 从 ChatDetailActivity 内部类提取。
 * 依赖 Context 提供 dp() 和 getColor()。
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public ChatHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16), dp(8), dp(16), dp(8));
        return new ChatHolder(row);
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ChatHolder extends RecyclerView.ViewHolder {
        private final LinearLayout container;
        private final TextView bubble;
        private final TextView time;

        ChatHolder(LinearLayout itemView) {
            super(itemView);
            container = itemView;
            bubble = new TextView(itemView.getContext());
            bubble.setTextSize(16);
            bubble.setLineSpacing(dp(4), 1f);
            bubble.setPadding(dp(16), dp(12), dp(16), dp(12));
            time = new TextView(itemView.getContext());
            time.setTextSize(12);
            time.setTextColor(context.getColor(R.color.text_secondary));
            container.addView(bubble);
            container.addView(time);
        }

        void bind(ChatMessage msg) {
            container.setGravity(msg.isUser() ? Gravity.END : Gravity.START);
            bubble.setText(msg.content);
            bubble.setTextColor(context.getColor(R.color.text_primary));
            bubble.setBackgroundResource(msg.isUser()
                ? R.drawable.bg_chat_user_bubble : R.drawable.bg_chat_ai_bubble);
            time.setText(msg.time);
        }
    }

    private int dp(int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }
}
