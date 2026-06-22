package com.silverguardian.prototype.models;

// 记忆数据模型：标题、内容、分类、创建时间（清单中名为Memory.java）
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
