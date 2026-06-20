package com.silverguardian.prototype.models;
public class AlbumPhoto {
 public int id; public String url,publicUri,title,description,category,uploadedBy,sceneTag,familyMessage; public boolean favorite;
 public AlbumPhoto(int id,String url,String title,String description,String category,String uploadedBy,boolean favorite,String sceneTag,String familyMessage){this(id,url,"",title,description,category,uploadedBy,favorite,sceneTag,familyMessage);}
 public AlbumPhoto(int id,String url,String publicUri,String title,String description,String category,String uploadedBy,boolean favorite,String sceneTag,String familyMessage){this.id=id;this.url=url;this.publicUri=publicUri==null?"":publicUri;this.title=title;this.description=description;this.category=category;this.uploadedBy=uploadedBy;this.favorite=favorite;this.sceneTag=sceneTag;this.familyMessage=familyMessage;}
}