package com.silverguardian.prototype.models;

public class User {
    public int id;
    public String name;
    public String pin;
    public String avatar; // local asset path
    public int age;
    public String healthConditions;

    public User(int id, String name, String pin, String avatar, int age, String healthConditions) {
        this.id = id;
        this.name = name;
        this.pin = pin;
        this.avatar = avatar;
        this.age = age;
        this.healthConditions = healthConditions;
    }
}
