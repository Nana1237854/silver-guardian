package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.silverguardian.prototype.PhotoDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.AlbumGroup;
import com.silverguardian.prototype.models.AlbumPhoto;
import com.silverguardian.prototype.utils.GalleryPermissionHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 亲情相册  —  GalleryPermissionHelper 处理权限, AlbumGroup 管理分组。
 * 交互: 相册列表 ⇄ 照片网格, 长按删除相册, 创建后直接进入。
 */
public class AlbumFragment extends BaseFragment {

    private GalleryPermissionHelper permHelper;
    private LinearLayout root;
    private Uri pendingImageUri;
    private String currentAlbum;

    private final List<AlbumPhoto> allPhotos   = new ArrayList<>();
    private final List<AlbumGroup>  albumGroups = new ArrayList<>();
    private final List<AlbumPhoto>  albumPhotos = new ArrayList<>();

    // ===== lifecycle =====

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(color(R.color.bg_page));
        root.setPadding(dp(16), dp(16), dp(16), dp(110));
        permHelper = new GalleryPermissionHelper(this);
        buildAlbumList();
        return root;
    }

    // ===== album list screen =====

    private void buildAlbumList() {
        root.removeAllViews();
        currentAlbum = null;

        // --- header ---
        LinearLayout h = hRow();
        h.addView(txt("📸 亲情相册", 24, true), lp(0, -2, 1));
        TextView add = chip("＋ 创建相册"); add.setOnClickListener(v -> askCreateAlbum());
        h.addView(add);  root.addView(h);

        // --- data ---
        allPhotos.clear(); allPhotos.addAll(MockData.getPhotos());
        albumGroups.clear();
        albumGroups.add(new AlbumGroup(AlbumGroup.ALL_PHOTOS, allPhotos));
        Map<String,List<AlbumPhoto>> map = new LinkedHashMap<>();
        for (AlbumPhoto p : allPhotos)
            map.computeIfAbsent(p.category != null ? p.category : "其他", k -> new ArrayList<>()).add(p);
        for (Map.Entry<String,List<AlbumPhoto>> e : map.entrySet())
            albumGroups.add(new AlbumGroup(e.getKey(), e.getValue()));

        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rv.setAdapter(new AlbumListAdapter());
        root.addView(rv, lp(-1, 0, 1));
    }

    // ===== photo grid screen =====

    private void buildPhotoGrid(String album) {
        root.removeAllViews();
        currentAlbum = album;

        LinearLayout h = hRow();
        TextView back = chip("← 返回"); back.setOnClickListener(v -> buildAlbumList()); h.addView(back);
        h.addView(txt(album, 22, true), lp(0, -2, 1));
        TextView up = chip("📷 上传照片"); up.setOnClickListener(v -> askUploadPhoto()); h.addView(up);
        root.addView(h);

        albumPhotos.clear();
        for (AlbumPhoto p : allPhotos) if (album.equals(p.category)) albumPhotos.add(p);

        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rv.setAdapter(new PhotoGridAdapter());
        root.addView(rv, lp(-1, 0, 1));
    }

    // ===== dialogs =====

    private void askCreateAlbum() {
        EditText et = edit("相册名称，例如：公园散步");
        new AlertDialog.Builder(requireContext())
            .setTitle("创建相册").setView(et)
            .setPositiveButton("创建", (d,w) -> {
                String n = et.getText().toString().trim();
                if (!n.isEmpty()) buildPhotoGrid(n);
            }).setNegativeButton("取消", null).show();
    }

    private void askUploadPhoto() {
        pendingImageUri = null;
        LinearLayout f = new LinearLayout(requireContext());
        f.setOrientation(LinearLayout.VERTICAL); f.setPadding(dp(36), dp(8), dp(36), 0);

        ImageView preview = new ImageView(requireContext());
        preview.setLayoutParams(new LinearLayout.LayoutParams(dp(200), dp(200)));
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundColor(0xFFE5ECE7);
        preview.setImageResource(android.R.drawable.ic_menu_gallery);
        f.addView(preview);

        TextView pick = chip("📷 从相册选择照片");
        f.addView(pick, new LinearLayout.LayoutParams(-1, -2));
        EditText titleEt = edit("照片标题");
        EditText msgEt   = edit("家属留言");
        f.addView(titleEt); f.addView(msgEt);

        pick.setOnClickListener(v -> permHelper.requestPermissionThen(permHelper::openGallery));

        new AlertDialog.Builder(requireContext())
            .setTitle("上传照片到「" + currentAlbum + "」").setView(f)
            .setPositiveButton("保存", (d,w) -> {
                String t = titleEt.getText().toString().trim();
                if (t.isEmpty()) t = "新照片";
                String u = pendingImageUri != null ? pendingImageUri.toString() : "";
                MockData.addPhoto(t, currentAlbum, msgEt.getText().toString().trim());
                if (!u.isEmpty()) {
                    AlbumPhoto added = MockData.getPhotos().isEmpty() ? null : MockData.getPhotos().get(0);
                    if (added != null) added.url = u;
                }
                buildPhotoGrid(currentAlbum);
                Toast.makeText(requireContext(), "照片已上传", Toast.LENGTH_SHORT).show();
            }).setNegativeButton("取消", null).show();
    }

    // ===== activity callbacks → delegate to GalleryPermissionHelper =====

    @Override
    public void onActivityResult(int rc, int result, @Nullable Intent data) {
        super.onActivityResult(rc, result, data);
        Uri u = GalleryPermissionHelper.handleActivityResult(rc, result, data);
        if (u != null) { pendingImageUri = u; toast("已选择照片"); }
    }

    @Override
    public void onRequestPermissionsResult(int rc, @NonNull String[] p, @NonNull int[] gr) {
        if (!permHelper.onRequestPermissionsResult(rc, gr))
            super.onRequestPermissionsResult(rc, p, gr);
    }

    // ===== adapters =====

    private class AlbumListAdapter extends RecyclerView.Adapter<AlbumHolder> {
        @NonNull @Override public AlbumHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new AlbumHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_photo, p, false));
        }
        @Override public void onBindViewHolder(@NonNull AlbumHolder h, int pos) { h.bind(albumGroups.get(pos)); }
        @Override public int getItemCount() { return albumGroups.size(); }
    }

    private class AlbumHolder extends RecyclerView.ViewHolder {
        TextView title, count; ImageView img;
        AlbumHolder(View v) {
            super(v);
            title = v.findViewById(R.id.photo_title);
            count = v.findViewById(R.id.photo_category);
            v.findViewById(R.id.photo_message).setVisibility(View.GONE);
            v.findViewById(R.id.photo_favorite).setVisibility(View.GONE);
            img = v.findViewById(R.id.photo_image);
        }
        void bind(AlbumGroup g) {
            title.setText(g.name);
            count.setText(g.count + " 张照片");
            loadImg(img, g.cover);
            itemView.setOnClickListener(v -> buildPhotoGrid(g.name));
            if (!AlbumGroup.ALL_PHOTOS.equals(g.name)) {
                itemView.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("删除相册「" + g.name + "」")
                        .setMessage("将删除所有照片，确定吗？")
                        .setPositiveButton("删除", (d,w) -> {
                            for (AlbumPhoto p : new ArrayList<>(allPhotos))
                                if (g.name.equals(p.category)) MockData.deletePhoto(p);
                            buildAlbumList();
                        }).setNegativeButton("取消", null).show();
                    return true;
                });
            }
        }
    }

    private class PhotoGridAdapter extends RecyclerView.Adapter<PhotoHolder> {
        @NonNull @Override public PhotoHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new PhotoHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_photo, p, false));
        }
        @Override public void onBindViewHolder(@NonNull PhotoHolder h, int pos) { h.bind(albumPhotos.get(pos)); }
        @Override public int getItemCount() { return albumPhotos.size(); }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        TextView title, msg, fav; ImageView img;
        PhotoHolder(View v) {
            super(v);
            title = v.findViewById(R.id.photo_title);
            v.findViewById(R.id.photo_category).setVisibility(View.GONE);
            msg = v.findViewById(R.id.photo_message);
            fav = v.findViewById(R.id.photo_favorite);
            img = v.findViewById(R.id.photo_image);
        }
        void bind(AlbumPhoto p) {
            title.setText(p.title); msg.setText(p.familyMessage);
            fav.setVisibility(p.favorite ? View.VISIBLE : View.GONE);
            loadImg(img, p);
            itemView.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), PhotoDetailActivity.class);
                i.putExtra("photo_title", p.title);   i.putExtra("photo_url", p.url);
                i.putExtra("photo_category", p.category); i.putExtra("photo_message", p.familyMessage);
                i.putExtra("photo_scene_tag", p.sceneTag); i.putExtra("photo_description", p.description);
                i.putExtra("photo_favorite", p.favorite);
                i.putExtra("photo_position", getAdapterPosition());
                i.putExtra("photo_total", albumPhotos.size());
                startActivity(i);
            });
            itemView.setOnLongClickListener(v -> {
                MockData.toggleFavorite(p); buildPhotoGrid(currentAlbum); return true;
            });
        }
    }

    // shared Glide loader (eliminates duplication between AlbumHolder & PhotoHolder)
    private void loadImg(ImageView iv, AlbumPhoto p) {
        if (p != null && p.url != null && !p.url.isEmpty())
            try { Glide.with(iv).load(Uri.parse(p.url)).centerCrop().into(iv); }
            catch (Exception e) { iv.setImageResource(android.R.drawable.ic_menu_gallery); }
        else iv.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    // ===== view builders =====

    private LinearLayout hRow() {
        LinearLayout h = new LinearLayout(requireContext());
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(0, 0, 0, dp(16));
        return h;
    }

    private TextView txt(String s, int sz, boolean b) {
        TextView v = new TextView(requireContext());
        v.setText(s); v.setTextSize(sz);
        if (b) v.setTypeface(null, android.graphics.Typeface.BOLD);
        v.setTextColor(color(R.color.text_primary));
        return v;
    }

    private TextView chip(String s) {
        TextView v = txt(s, 15, true);
        v.setTextColor(color(R.color.primary));
        v.setBackgroundResource(R.drawable.bg_chip_soft);
        v.setPadding(dp(14), dp(9), dp(14), dp(9));
        return v;
    }

    private EditText edit(String hint) {
        EditText e = new EditText(requireContext());
        e.setHint(hint); e.setTextSize(18);
        return e;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float wt) {
        return new LinearLayout.LayoutParams(w, h, wt);
    }

}
