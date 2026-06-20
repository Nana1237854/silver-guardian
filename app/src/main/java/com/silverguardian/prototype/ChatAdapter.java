package com.silverguardian.prototype;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.ChatMessage;
import com.silverguardian.prototype.reminder.TtsHelper;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private static final int VIEW_TYPE_AI = 0;
    private static final int VIEW_TYPE_USER = 1;

    private final List<ChatMessage> messages;
    private final LayoutInflater inflater;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.inflater = LayoutInflater.from(context);
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == VIEW_TYPE_USER
            ? R.layout.item_chat_message_user
            : R.layout.item_chat_message_ai;
        return new ChatHolder(inflater.inflate(layoutRes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatHolder extends RecyclerView.ViewHolder {
        private final TextView bubble;
        private final TextView time;
        private final ImageView replayButton;

        ChatHolder(View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.chat_bubble);
            time = itemView.findViewById(R.id.chat_time);
            replayButton = itemView.findViewById(R.id.chat_replay);
        }

        void bind(ChatMessage message) {
            bubble.setText(message.content);
            time.setText(message.time);
            if (replayButton != null) {
                replayButton.setOnClickListener(v -> TtsHelper.speak(message.content));
            }
        }
    }
}
