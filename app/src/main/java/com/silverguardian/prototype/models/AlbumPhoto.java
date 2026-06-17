package com.silverguardian.prototype.models;

public class AlbumPhoto {
    public int id;
    public String url;
    public String title;
    public String description;
    public String category;
    public String uploadedBy;
    public boolean favorite;
    public String sceneTag;
    public String familyMessage;

    public AlbumPhoto(int id, String url, String title, String description, String category, String uploadedBy, boolean favorite, String sceneTag, String familyMessage) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.description = description;
        this.category = category;
        this.uploadedBy = uploadedBy;
        this.favorite = favorite;
        this.sceneTag = sceneTag;
        this.familyMessage = familyMessage;
    }
}
