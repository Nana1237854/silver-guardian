package com.silverguardian.prototype.models;

import java.util.List;

/**
 * 相册分组：一个分类 = 一个相册。
 * 同时作为相册列表和照片网格的数据模型。
 */
public class AlbumGroup {
    public static final String ALL_PHOTOS = "全部照片";

    public String name;
    public int count;
    public AlbumPhoto cover;

    public AlbumGroup(String name, List<AlbumPhoto> photos) {
        this.name = name;
        this.count = photos.size();
        this.cover = photos.isEmpty() ? null : photos.get(0);
    }
}
