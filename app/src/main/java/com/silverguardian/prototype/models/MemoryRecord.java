package com.silverguardian.prototype.models;

public class MemoryRecord {
    public int id;
    public String content;
    public String category;
    public String createdAt;

    public MemoryRecord(int id, String content, String category, String createdAt) {
        this.id = id;
        this.content = content;
        this.category = category;
        this.createdAt = createdAt;
    }
}
