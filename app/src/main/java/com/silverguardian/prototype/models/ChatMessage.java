package com.silverguardian.prototype.models;

public class ChatMessage {
    public static final String TYPE_USER = "user";
    public static final String TYPE_AI = "ai";

    public String content;
    public String type;  // "user" or "ai"
    public String time;

    public ChatMessage(String content, String type, String time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public boolean isUser() {
        return TYPE_USER.equals(type);
    }
}
