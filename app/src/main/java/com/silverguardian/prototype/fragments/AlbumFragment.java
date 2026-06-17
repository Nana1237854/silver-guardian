package com.silverguardian.prototype.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.silverguardian.prototype.PhotoDetailActivity;
import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.AlbumPhoto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 亲情相册 — 两层结构：相册列表 → 点击进入 → 照片网格。
 */
public class AlbumFragment extends Fragment {
    private static final int PICK_IMAGE = 501;
    private static final int REQUEST_READ_IMAGES = 502;

    private LinearLayout root;
    private RecyclerView recyclerView;
    private View albumListView;
    private View photoGridView;

    private String currentAlbum = null;
    private Uri pendingImageUri;

    private final List<AlbumPhoto> allPhotos = new ArrayList<>();
    private final List<AlbumGroup> albumGroups = new ArrayList<>();
    private final List<AlbumPhoto> albumPhotos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(R.color.bg_page));
        root.setPadding(dp(16), dp(16), dp(16), dp(110));

        showAlbumList();
        return root;
    }

    // ========== 相册列表视图 ==========

    private void showAlbumList() {
        root.removeAllViews();
        currentAlbum = null;

        // 头部
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        TextView title = new TextView(requireContext());
        title.setText("📸 亲情相册");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getColor(R.color.text_primary));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView createBtn = chip("＋ 创建相册");
        createBtn.setOnClickListener(v -> showCreateAlbumDialog());
        header.addView(createBtn);

        root.addView(header);

        // 相册网格
        allPhotos.clear();
        allPhotos.addAll(MockData.getPhotos());

        albumGroups.clear();
        Map<String, List<AlbumPhoto>> grouped = new LinkedHashMap<>();
        for (AlbumPhoto p : allPhotos) {
            String cat = p.category != null ? p.category : "其他";
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<String, List<AlbumPhoto>> e : grouped.entrySet()) {
            albumGroups.add(new AlbumGroup(e.getKey(), e.getValue()));
        }

        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new AlbumGroupAdapter());
        root.addView(recyclerView, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void showCreateAlbumDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("相册名称，例如：公园散步");
        input.setTextSize(18);
        input.setPadding(dp(16), dp(12), dp(16), dp(12));
        new AlertDialog.Builder(requireContext())
            .setTitle("创建相册")
            .setView(input)
            .setPositiveButton("创建", (d, w) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    MockData.addPhoto("新照片", name, "");
                    showAlbumList();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    // ========== 相册内照片视图 ==========

    private void showPhotoGrid(String albumName) {
        root.removeAllViews();
        currentAlbum = albumName;

        // 头部
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(16));

        TextView backBtn = new TextView(requireContext());
        backBtn.setText("← 返回");
        backBtn.setTextSize(18);
        backBtn.setTextColor(getColor(R.color.primary));
        backBtn.setBackgroundResource(R.drawable.bg_chip_soft);
        backBtn.setPadding(dp(14), dp(8), dp(14), dp(8));
        backBtn.setOnClickListener(v -> showAlbumList());
        header.addView(backBtn);

        TextView title = new TextView(requireContext());
        title.setText(albumName);
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getColor(R.color.text_primary));
        title.setPadding(dp(10), 0, 0, 0);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        TextView uploadBtn = chip("📷 上传照片");
        uploadBtn.setOnClickListener(v -> showUploadDialog());
        header.addView(uploadBtn);

        root.addView(header);

        // 照片网格
        albumPhotos.clear();
        for (AlbumPhoto p : allPhotos) {
            if (albumName.equals(p.category)) albumPhotos.add(p);
        }

        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(new PhotoGridAdapter());
        root.addView(recyclerView, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    // ========== 上传照片 ==========

    private void showUploadDialog() {
        pendingImageUri = null;
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(36, 8, 36, 0);

        ImageView preview = new ImageView(requireContext());
        preview.setLayoutParams(new LinearLayout.LayoutParams(dp(200), dp(200)));
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundColor(0xFFE5ECE7);
        preview.setImageResource(android.R.drawable.ic_menu_gallery);
        form.addView(preview);

        TextView pickBtn = new TextView(requireContext());
        pickBtn.setText("📷 从相册选择照片");
        pickBtn.setTextSize(18);
        pickBtn.setTextColor(getColor(R.color.primary));
        pickBtn.setGravity(Gravity.CENTER);
        pickBtn.setBackgroundResource(R.drawable.bg_chip_soft);
        pickBtn.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(12);
        pickBtn.setLayoutParams(p);
        form.addView(pickBtn);

        EditText titleInput = new EditText(requireContext());
        titleInput.setHint("照片标题");
        titleInput.setTextSize(18);
        form.addView(titleInput);

        EditText msgInput = new EditText(requireContext());
        msgInput.setHint("家属留言");
        msgInput.setTextSize(18);
        form.addView(msgInput);

        pickBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_IMAGES);
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_IMAGES);
                    return;
                }
            }
            openGallery();
        });

        new AlertDialog.Builder(requireContext())
            .setTitle("上传照片到「" + currentAlbum + "」")
            .setView(form)
            .setPositiveButton("保存", (d, w) -> {
                String pTitle = titleInput.getText().toString().trim();
                if (pTitle.isEmpty()) pTitle = "新照片";
                String imagePath = pendingImageUri != null ? pendingImageUri.toString() : "";
                MockData.addPhoto(pTitle, currentAlbum, msgInput.getText().toString().trim());
                if (!imagePath.isEmpty()) {
                    AlbumPhoto added = MockData.getPhotos().isEmpty() ? null : MockData.getPhotos().get(0);
                    if (added != null) added.url = imagePath;
                }
                showPhotoGrid(currentAlbum);
                Toast.makeText(requireContext(), "照片已上传", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void openGallery() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null) {
            pendingImageUri = data.getData();
            Toast.makeText(requireContext(), "已选择照片", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) openGallery();
            else Toast.makeText(requireContext(), "需要相册权限才能在系统设置中授权", Toast.LENGTH_LONG).show();
        }
    }

    // ========== Adapters ==========

    private class AlbumGroupAdapter extends RecyclerView.Adapter<AlbumGroupHolder> {
        @NonNull @Override public AlbumGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new AlbumGroupHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull AlbumGroupHolder h, int pos) { h.bind(albumGroups.get(pos)); }
        @Override public int getItemCount() { return albumGroups.size(); }
    }

    private class AlbumGroupHolder extends RecyclerView.ViewHolder {
        TextView title, category, message, favorite;
        ImageView image;
        AlbumGroupHolder(View v) {
            super(v);
            title = v.findViewById(R.id.photo_title);
            category = v.findViewById(R.id.photo_category);
            message = v.findViewById(R.id.photo_message);
            favorite = v.findViewById(R.id.photo_favorite);
            image = v.findViewById(R.id.photo_image);
            favorite.setVisibility(View.GONE);
        }
        void bind(AlbumGroup group) {
            title.setText(group.name);
            category.setText(group.count + " 张照片");
            message.setText("");
            AlbumPhoto cover = group.recent;
            if (cover != null && cover.url != null && !cover.url.isEmpty()) {
                try { Glide.with(image).load(Uri.parse(cover.url)).centerCrop().into(image); }
                catch (Exception e) { image.setImageResource(android.R.drawable.ic_menu_gallery); }
            } else {
                image.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            itemView.setOnClickListener(v -> showPhotoGrid(group.name));
        }
    }

    private class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridHolder> {
        @NonNull @Override public PhotoGridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoGridHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull PhotoGridHolder h, int pos) { h.bind(albumPhotos.get(pos)); }
        @Override public int getItemCount() { return albumPhotos.size(); }
    }

    private class PhotoGridHolder extends RecyclerView.ViewHolder {
        TextView title, category, message, favorite;
        ImageView image;
        PhotoGridHolder(View v) {
            super(v);
            title = v.findViewById(R.id.photo_title);
            category = v.findViewById(R.id.photo_category);
            message = v.findViewById(R.id.photo_message);
            favorite = v.findViewById(R.id.photo_favorite);
            image = v.findViewById(R.id.photo_image);
        }
        void bind(AlbumPhoto photo) {
            title.setText(photo.title);
            category.setVisibility(View.GONE);
            message.setText(photo.familyMessage);
            favorite.setVisibility(photo.favorite ? View.VISIBLE : View.GONE);
            if (photo.url != null && !photo.url.isEmpty()) {
                try { Glide.with(image).load(Uri.parse(photo.url)).centerCrop().into(image); }
                catch (Exception e) { image.setImageResource(android.R.drawable.ic_menu_gallery); }
            } else {
                image.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PhotoDetailActivity.class);
                intent.putExtra("photo_title", photo.title);
                intent.putExtra("photo_url", photo.url);
                intent.putExtra("photo_category", photo.category);
                intent.putExtra("photo_message", photo.familyMessage);
                intent.putExtra("photo_scene_tag", photo.sceneTag);
                intent.putExtra("photo_description", photo.description);
                intent.putExtra("photo_favorite", photo.favorite);
                startActivity(intent);
            });
            itemView.setOnLongClickListener(v -> {
                MockData.toggleFavorite(photo);
                showPhotoGrid(currentAlbum);
                return true;
            });
        }
    }

    // ========== Model ==========

    private static class AlbumGroup {
        String name;
        int count;
        AlbumPhoto recent;
        AlbumGroup(String name, List<AlbumPhoto> photos) {
            this.name = name;
            this.count = photos.size();
            this.recent = photos.isEmpty() ? null : photos.get(0);
        }
    }

    // ========== Utils ==========

    private TextView chip(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(15);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(14), dp(9), dp(14), dp(9));
        return view;
    }

    private int getColor(int resId) { return getResources().getColor(resId); }
    private int dp(int value) { return Math.round(getResources().getDisplayMetrics().density * value); }
}
