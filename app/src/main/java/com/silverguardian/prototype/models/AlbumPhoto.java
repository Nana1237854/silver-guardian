package com.silverguardian.prototype.models;

// 照片数据模型：照片路径、标题、留言、收藏状态
public class AlbumPhoto {
    public int id;
    public int albumId;
    public String url;
    public String publicUri;
    public String title;
    public String description;
    public String category;
    public String uploadedBy;
    public String sceneTag;
    public String familyMessage;
    public boolean favorite;

    public AlbumPhoto(int id, String url, String title, String description, String category,
                      String uploadedBy, boolean favorite, String sceneTag, String familyMessage) {
        this(id, 0, url, "", title, description, category, uploadedBy, favorite, sceneTag, familyMessage);
    }

    public AlbumPhoto(int id, String url, String publicUri, String title, String description,
                      String category, String uploadedBy, boolean favorite, String sceneTag,
                      String familyMessage) {
        this(id, 0, url, publicUri, title, description, category, uploadedBy, favorite, sceneTag, familyMessage);
    }

    public AlbumPhoto(int id, int albumId, String url, String publicUri, String title, String description,
                      String category, String uploadedBy, boolean favorite, String sceneTag,
                      String familyMessage) {
        this.id = id;
        this.albumId = albumId;
        this.url = url;
        this.publicUri = publicUri == null ? "" : publicUri;
        this.title = title;
        this.description = description;
        this.category = category;
        this.uploadedBy = uploadedBy;
        this.favorite = favorite;
        this.sceneTag = sceneTag;
        this.familyMessage = familyMessage;
    }
}