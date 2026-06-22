package com.silverguardian.prototype;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

// 照片详情页：大图查看、标题/留言/收藏展示
public class PhotoDetailActivity extends BaseActivity {

    // 初始化照片详情页：接收Intent参数，渲染大图与信息卡片
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        String title = getIntent().getStringExtra("photo_title");
        String url = getIntent().getStringExtra("photo_url");
        String category = getIntent().getStringExtra("photo_category");
        String message = getIntent().getStringExtra("photo_message");
        String sceneTag = getIntent().getStringExtra("photo_scene_tag");
        String description = getIntent().getStringExtra("photo_description");
        boolean favorite = getIntent().getBooleanExtra("photo_favorite", false);
        int position = getIntent().getIntExtra("photo_position", -1);
        int totalCount = getIntent().getIntExtra("photo_total", 1);

        bindHeader();
        bindImage(url);
        bindContent(
            title == null || title.trim().isEmpty() ? getString(R.string.photo_detail_default_title) : title,
            category,
            sceneTag,
            description,
            message,
            favorite,
            position,
            totalCount
        );
    }

    private void bindHeader() {
        View header = findViewById(R.id.photo_detail_header);
        TextView back = header.findViewById(R.id.header_back);
        TextView title = header.findViewById(R.id.header_title);
        TextView action = header.findViewById(R.id.header_action);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        title.setText(R.string.photo_detail_default_title);
        action.setVisibility(View.GONE);
    }

    private void bindImage(String url) {
        ImageView imageView = findViewById(R.id.photo_detail_image);
        if (url != null && !url.isEmpty()) {
            try {
                Glide.with(this)
                    .load(Uri.parse(url))
                    .placeholder(R.drawable.family_companion)
                    .error(R.drawable.family_companion)
                    .into(imageView);
                return;
            } catch (Exception ignored) {
            }
        }
        imageView.setImageResource(R.drawable.family_companion);
    }

    private void bindContent(String titleText, String category, String sceneTag, String description,
                             String message, boolean favorite, int position, int totalCount) {
        TextView pageCounter = findViewById(R.id.photo_detail_page_counter);
        TextView titleView = findViewById(R.id.photo_detail_title);
        TextView favoriteTag = findViewById(R.id.photo_detail_favorite_tag);
        View infoSection = findViewById(R.id.photo_detail_info_section);
        TextView infoTitle = infoSection.findViewById(R.id.section_card_title);
        LinearLayout infoContainer = infoSection.findViewById(R.id.section_card_content);

        if (position >= 0 && totalCount > 0) {
            pageCounter.setVisibility(View.VISIBLE);
            pageCounter.setText(getString(R.string.photo_detail_page_counter, position + 1, totalCount));
        } else {
            pageCounter.setVisibility(View.GONE);
        }

        titleView.setText(titleText);
        favoriteTag.setVisibility(favorite ? View.VISIBLE : View.GONE);
        findViewById(R.id.photo_detail_back_button).setOnClickListener(v -> finish());

        infoTitle.setText(R.string.photo_detail_info_section);
        infoContainer.removeAllViews();
        addInfoRowIfPresent(infoContainer, R.string.photo_detail_label_category, category);
        addInfoRowIfPresent(infoContainer, R.string.photo_detail_label_scene, sceneTag);
        addInfoRowIfPresent(infoContainer, R.string.photo_detail_label_description, description);
        addInfoRowIfPresent(infoContainer, R.string.photo_detail_label_message, message);
    }

    private void addInfoRowIfPresent(LinearLayout container, int labelRes, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        View row = LayoutInflater.from(this).inflate(R.layout.view_detail_info_row, container, false);
        ((TextView) row.findViewById(R.id.detail_info_label)).setText(getString(labelRes) + getString(R.string.detail_info_suffix));
        ((TextView) row.findViewById(R.id.detail_info_value)).setText(value);
        container.addView(row);
    }
}