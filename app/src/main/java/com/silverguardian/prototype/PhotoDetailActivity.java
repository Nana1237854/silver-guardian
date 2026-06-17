package com.silverguardian.prototype;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PhotoDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra("photo_title");
        String url = getIntent().getStringExtra("photo_url");
        String category = getIntent().getStringExtra("photo_category");
        String message = getIntent().getStringExtra("photo_message");
        String sceneTag = getIntent().getStringExtra("photo_scene_tag");
        String description = getIntent().getStringExtra("photo_description");
        boolean favorite = getIntent().getBooleanExtra("photo_favorite", false);
        int position = getIntent().getIntExtra("photo_position", -1);
        int totalCount = getIntent().getIntExtra("photo_total", 1);

        if (title == null) title = "照片详情";

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(48));
        scroll.addView(root);

        // 翻页导航
        if (position >= 0 && totalCount > 0) {
            LinearLayout navRow = new LinearLayout(this);
            navRow.setOrientation(LinearLayout.HORIZONTAL);
            navRow.setGravity(Gravity.CENTER);
            navRow.setPadding(0, 0, 0, dp(12));

            TextView navText = new TextView(this);
            navText.setText((position + 1) + " / " + totalCount);
            navText.setTextSize(16);
            navText.setTextColor(getColor(R.color.text_secondary));
            navText.setGravity(Gravity.CENTER);
            navRow.addView(navText, new LinearLayout.LayoutParams(0, -2, 1));

            root.addView(navRow);
        }

        // 图片
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(getColor(R.color.mint_soft));

        if (url != null && !url.isEmpty()) {
            try {
                Glide.with(this).load(Uri.parse(url)).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(imageView);
            } catch (Exception e) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        root.addView(imageView, new LinearLayout.LayoutParams(-1, dp(300)));

        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(24);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(getColor(R.color.text_primary));
        titleView.setPadding(0, dp(20), 0, dp(8));
        root.addView(titleView);

        if (favorite) {
            TextView favTag = new TextView(this);
            favTag.setText("❤️ 已收藏");
            favTag.setTextSize(14);
            favTag.setTextColor(getColor(R.color.favorite_pink));
            favTag.setBackgroundResource(R.drawable.bg_tag);
            favTag.setPadding(dp(10), dp(4), dp(10), dp(4));
            root.addView(favTag);
        }

        if (category != null && !category.isEmpty()) root.addView(infoRow("分类", category));
        if (sceneTag != null && !sceneTag.isEmpty()) root.addView(infoRow("场景", sceneTag));
        if (description != null && !description.isEmpty()) root.addView(infoRow("说明", description));
        if (message != null && !message.isEmpty()) root.addView(infoRow("家属留言", message));

        // 返回
        TextView back = new TextView(this);
        back.setText("↩ 返回相册");
        back.setTextSize(18);
        back.setGravity(Gravity.CENTER);
        back.setTextColor(getColor(R.color.primary));
        back.setBackgroundResource(R.drawable.bg_chip_soft);
        back.setPadding(0, dp(14), 0, dp(14));
        back.setOnClickListener(v -> finish());
        root.addView(back, new LinearLayout.LayoutParams(-1, -2));

        setContentView(scroll);
    }

    private View infoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, 0);
        TextView lv = new TextView(this);
        lv.setText(label + "：");
        lv.setTextSize(15);
        lv.setTextColor(getColor(R.color.text_secondary));
        row.addView(lv);
        TextView vv = new TextView(this);
        vv.setText(value);
        vv.setTextSize(15);
        vv.setTextColor(getColor(R.color.text_primary));
        vv.setPadding(dp(8), 0, 0, 0);
        row.addView(vv);
        return row;
    }

    private int dp(int val) { return Math.round(getResources().getDisplayMetrics().density * val); }
}
