package com.silverguardian.prototype.models;

public class FamilyMember {
    public int id;
    public String name;
    public String relationship;
    public String phone;
    public String avatar;
    public boolean online;

    public FamilyMember(int id, String name, String relationship, String phone, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.avatar = avatar;
        this.online = online;
    }
}
