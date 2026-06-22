package com.silverguardian.prototype.models;

// 相册数据模型：相册ID、名称、封面、照片数量
public class Album {
    public int id;
    public String name;
    public String coverUrl;
    public int photoCount;
    public String createdAt;

    public Album(int id, String name, String coverUrl, int photoCount, String createdAt) {
        this.id = id;
        this.name = name;
        this.coverUrl = coverUrl == null ? "" : coverUrl;
        this.photoCount = photoCount;
        this.createdAt = createdAt == null ? "" : createdAt;
    }
}