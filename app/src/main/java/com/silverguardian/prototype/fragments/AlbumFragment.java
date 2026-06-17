package com.silverguardian.prototype.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;
import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.AlbumPhoto;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {
    private final List<AlbumPhoto> visible = new ArrayList<>();
    private PhotoAdapter adapter;
    private Spinner categorySpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));
        root.setPadding(dp(16), dp(16), dp(16), dp(110));

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView title = title("亲情相册");
        title.setTextSize(26);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        categorySpinner = new Spinner(requireContext());
        header.addView(categorySpinner, new LinearLayout.LayoutParams(dp(150), -2));

        TextView upload = chip("上传照片");
        header.addView(upload, new LinearLayout.LayoutParams(-2, -2));

        RecyclerView grid = new RecyclerView(requireContext());
        grid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new PhotoAdapter();
        grid.setAdapter(adapter);
        root.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1));

        refreshCategories();
        upload.setOnClickListener(v -> showUploadDialog());
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { refresh(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        refresh();
        return root;
    }

    private void refreshCategories() {
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(MockData.photoCategories())));
    }

    private void refresh() {
        visible.clear();
        String selected = categorySpinner.getSelectedItem() == null ? "全部" : categorySpinner.getSelectedItem().toString();
        for (AlbumPhoto photo : MockData.getPhotos()) {
            if ("全部".equals(selected) || photo.category.equals(selected)) visible.add(photo);
        }
        adapter.notifyDataSetChanged();
    }

    private void showUploadDialog() {
        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(36, 8, 36, 0);
        EditText title = input("照片标题");
        Spinner category = new Spinner(requireContext());
        category.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"家庭", "旅行", "日常", "节日"}));
        EditText message = input("家属留言");
        form.addView(title);
        form.addView(category);
        form.addView(message);
        new AlertDialog.Builder(requireContext())
            .setTitle("上传照片")
            .setView(form)
            .setPositiveButton("保存", (d, w) -> {
                String photoTitle = title.getText().toString().trim();
                if (photoTitle.isEmpty()) photoTitle = "新的回忆";
                MockData.addPhoto(photoTitle, category.getSelectedItem().toString(), message.getText().toString().trim());
                refreshCategories();
                refresh();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private EditText input(String hint) {
        EditText editText = new EditText(requireContext());
        editText.setHint(hint);
        editText.setTextSize(18);
        return editText;
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {
        @NonNull @Override public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull PhotoViewHolder h, int pos) { h.bind(visible.get(pos)); }
        @Override public int getItemCount() { return visible.size(); }
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, message, favorite, placeholder;
        PhotoViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.photo_title);
            category = v.findViewById(R.id.photo_category);
            message = v.findViewById(R.id.photo_message);
            favorite = v.findViewById(R.id.photo_favorite);
            placeholder = v.findViewById(R.id.photo_placeholder);
        }

        void bind(AlbumPhoto photo) {
            title.setText(photo.title);
            category.setText(photo.category + " · " + photo.uploadedBy);
            message.setText(photo.familyMessage);
            favorite.setVisibility(photo.favorite ? View.VISIBLE : View.GONE);
            placeholder.setText(photo.title.substring(0, 1));
            itemView.setOnClickListener(v -> showPhotoDetail(photo));
            itemView.setOnLongClickListener(v -> {
                MockData.toggleFavorite(photo);
                refresh();
                return true;
            });
        }
    }

    private void showPhotoDetail(AlbumPhoto photo) {
        new AlertDialog.Builder(requireContext())
            .setTitle(photo.title)
            .setMessage("分类：" + photo.category + "\n场景：" + photo.sceneTag + "\n说明：" + photo.description + "\n\n家属留言：\n" + photo.familyMessage)
            .setPositiveButton(photo.favorite ? "取消收藏" : "收藏", (d, w) -> {
                MockData.toggleFavorite(photo);
                refresh();
            })
            .setNegativeButton("关闭", null)
            .show();
    }

    private TextView title(String text) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(18);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setTextColor(getResources().getColor(R.color.text_primary));
        return view;
    }

    private TextView chip(String text) {
        TextView view = title(text);
        view.setTextSize(15);
        view.setTextColor(getResources().getColor(R.color.primary));
        view.setBackgroundResource(R.drawable.bg_chip_soft);
        view.setPadding(dp(14), dp(9), dp(14), dp(9));
        return view;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
