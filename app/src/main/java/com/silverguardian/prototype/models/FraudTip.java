package com.silverguardian.prototype.models;

public class FraudTip {
    public int id;
    public String title;
    public String category;
    public String content;
    public String action;

    public FraudTip(int id, String title, String category, String content, String action) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.content = content;
        this.action = action;
    }
}
