package com.silverguardian.prototype.models;

public class Medicine {
    public int id;
    public String name;
    public String type;
    public String time;
    public String method;
    public String description;
    public String imagePath;
    public boolean takenToday;

    public Medicine(int id, String name, String type, String time, String method, String description, String imagePath, boolean takenToday) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.time = time;
        this.method = method;
        this.description = description;
        this.imagePath = imagePath;
        this.takenToday = takenToday;
    }
}
