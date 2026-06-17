package com.silverguardian.prototype;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PhotoDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setTextSize(22);
        view.setPadding(32, 32, 32, 32);
        view.setText("照片详情\n\n当前原型主要通过相册卡片弹窗查看、收藏和留言。");
        setContentView(view);
    }
}
