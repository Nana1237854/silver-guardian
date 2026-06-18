package com.silverguardian.prototype;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), dp(7), dp(14), dp(7));
        return new ChatHolder(row);
    }

    @Override public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override public int getItemCount() { return messages.size(); }

    class ChatHolder extends RecyclerView.ViewHolder {
        private final LinearLayout row;

        ChatHolder(LinearLayout itemView) {
            super(itemView);
            row = itemView;
        }

        void bind(ChatMessage message) {
            row.removeAllViews();
            row.setGravity(message.isUser() ? Gravity.END | Gravity.TOP : Gravity.START | Gravity.TOP);

            ImageView avatar = new ImageView(context);
            avatar.setImageResource(message.isUser() ? R.drawable.elder_profile : R.drawable.ai_companion);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setContentDescription(message.isUser() ? "颜爷爷" : "银发守护 AI 助手");

            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(message.isUser() ? Gravity.END : Gravity.START);

            TextView bubble = new TextView(context);
            bubble.setText(message.content);
            bubble.setTextSize(17);
            bubble.setTextColor(context.getColor(R.color.text_primary));
            bubble.setLineSpacing(dp(4), 1f);
            bubble.setPadding(dp(16), dp(12), dp(16), dp(12));
            bubble.setMaxWidth(dp(286));
            bubble.setBackgroundResource(message.isUser()
                ? R.drawable.bg_chat_user_bubble : R.drawable.bg_chat_ai_bubble);
            content.addView(bubble);

            TextView time = new TextView(context);
            time.setText(message.time);
            time.setTextSize(14);
            time.setTextColor(context.getColor(R.color.text_secondary));
            time.setPadding(dp(8), dp(4), dp(8), 0);
            content.addView(time);

            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(42), dp(42));
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(0, -2, 1);
            if (message.isUser()) {
                contentParams.rightMargin = dp(10);
                row.addView(content, contentParams);
                row.addView(avatar, avatarParams);
            } else {
                contentParams.leftMargin = dp(10);
                row.addView(avatar, avatarParams);
                row.addView(content, contentParams);
            }
        }
    }

    private int dp(int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }
}
