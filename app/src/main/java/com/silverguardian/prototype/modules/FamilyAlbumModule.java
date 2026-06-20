package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.AlbumGroup;
import com.silverguardian.prototype.models.AlbumPhoto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FamilyAlbumModule {
    public static final String OTHER = "Other";

    private final Repository repository;

    public FamilyAlbumModule(Repository repository) {
        this.repository = repository;
    }

    public List<AlbumPhoto> getPhotos() { return new ArrayList<>(repository.getPhotos()); }

    public List<AlbumGroup> getAlbumGroups() {
        Map<String, List<AlbumPhoto>> grouped = new LinkedHashMap<>();
        for (AlbumPhoto photo : repository.getPhotos()) {
            String category = photo.category == null ? OTHER : photo.category;
            grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(photo);
        }
        List<AlbumGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<AlbumPhoto>> entry : grouped.entrySet()) {
            if (!AlbumGroup.ALL_PHOTOS.equals(entry.getKey())) groups.add(new AlbumGroup(entry.getKey(), entry.getValue()));
        }
        return groups;
    }

    public List<AlbumPhoto> getPhotosForAlbum(String album, String query, boolean newestFirst) {
        List<AlbumPhoto> visible = new ArrayList<>();
        String safeQuery = query == null ? "" : query;
        for (AlbumPhoto photo : repository.getPhotos()) {
            boolean inAlbum = AlbumGroup.ALL_PHOTOS.equals(album) || album.equals(photo.category);
            boolean matches = safeQuery.isEmpty() || (photo.title != null && photo.title.contains(safeQuery)) || (photo.familyMessage != null && photo.familyMessage.contains(safeQuery));
            if (inAlbum && matches) visible.add(photo);
        }
        if (!newestFirst) Collections.reverse(visible);
        return visible;
    }

    public AlbumPhoto addPhoto(String title, String category, String message) { return repository.addPhoto(title, category, message); }
    public AlbumPhoto addPhoto(String privatePath,String publicUri,String title,String category,String message){return repository.addPhoto(privatePath,publicUri,title,category,message);}
    public void deletePhoto(AlbumPhoto photo) { repository.deletePhoto(photo); }
    public void toggleFavorite(AlbumPhoto photo) { repository.toggleFavorite(photo); }
}