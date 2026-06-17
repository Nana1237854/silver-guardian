package com.silverguardian.prototype;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.BluetoothDeviceMock;

public class BluetoothActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        root.setBackgroundColor(getColor(R.color.bg_page));

        TextView title = new TextView(this);
        title.setText("蓝牙设备管理");
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        for (BluetoothDeviceMock device : MockData.getBluetoothDevices()) {
            TextView row = new TextView(this);
            row.setText(device.name + "\n" + device.status + " · 点击连接并上传 " + device.value);
            row.setTextSize(21);
            row.setPadding(24, 24, 24, 24);
            row.setBackgroundColor(getColor(R.color.surface_white));
            row.setOnClickListener(v -> connect(device));
            root.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        setContentView(root);
    }

    private void connect(BluetoothDeviceMock device) {
        MockData.addHealthData(device.type, device.value, "正常");
        new AlertDialog.Builder(this)
            .setTitle("已连接")
            .setMessage(device.name + " 已读取数据：" + device.value + "\n数据已写入健康档案。")
            .setPositiveButton("完成", null)
            .show();
    }
}
