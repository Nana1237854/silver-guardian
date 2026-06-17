package com.silverguardian.prototype;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.silverguardian.prototype.data.MockData;
import com.silverguardian.prototype.models.BluetoothDeviceMock;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝牙设备集成 — BLE 扫描 + 连接 + 读取健康数据。
 * 课程创新点：覆盖蓝牙 BLE + 运行时权限。
 * 真机使用 BluetoothLeScanner，模拟器降级为模拟设备。
 */
public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_BT_PERMISSIONS = 301;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private LinearLayout deviceList;
    private TextView statusText;
    private boolean isScanning = false;
    private final List<BluetoothDeviceMock> foundDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MockData.init(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.bg_page));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(48));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("蓝牙设备管理");
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getColor(R.color.text_primary));
        root.addView(title);

        statusText = new TextView(this);
        statusText.setText("正在初始化蓝牙...");
        statusText.setTextSize(15);
        statusText.setTextColor(getColor(R.color.text_secondary));
        statusText.setPadding(0, dp(8), 0, dp(16));
        root.addView(statusText);

        // 扫描按钮
        Button scanBtn = btn("🔍 扫描设备", R.drawable.bg_button_primary, getColor(R.color.surface_white));
        scanBtn.setOnClickListener(v -> startScan());
        root.addView(scanBtn);

        // 设备列表
        deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);
        root.addView(deviceList);

        // 模拟设备（作为 fallback）
        Button mockBtn = btn("📋 加载模拟设备", R.drawable.bg_chip_soft, getColor(R.color.primary));
        mockBtn.setOnClickListener(v -> loadMockDevices());
        root.addView(mockBtn);

        // 返回
        Button backBtn = btn("↩ 返回", R.drawable.bg_chip_soft, getColor(R.color.primary));
        backBtn.setOnClickListener(v -> finish());
        root.addView(backBtn);

        setContentView(scroll);

        initBluetooth();
    }

    private void initBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            statusText.setText("⚠️ 此设备不支持蓝牙 BLE，使用模拟模式");
            loadMockDevices();
            return;
        }

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (btManager == null) {
            statusText.setText("⚠️ 无法获取蓝牙服务，使用模拟模式");
            loadMockDevices();
            return;
        }

        bluetoothAdapter = btManager.getAdapter();
        if (bluetoothAdapter == null) {
            statusText.setText("⚠️ 此设备不支持蓝牙");
            loadMockDevices();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            statusText.setText("📱 请先开启蓝牙");
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            return;
        }

        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (leScanner == null) {
            statusText.setText("⚠️ BLE 不可用，使用模拟模式");
            loadMockDevices();
            return;
        }

        statusText.setText("✅ 蓝牙就绪，点击扫描设备");
    }

    private void startScan() {
        // Android 12+ 需要 BLUETOOTH_SCAN 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BT_PERMISSIONS);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BT_PERMISSIONS);
                return;
            }
        }

        if (leScanner == null) {
            Toast.makeText(this, "BLE 扫描器不可用，请使用模拟设备", Toast.LENGTH_SHORT).show();
            loadMockDevices();
            return;
        }

        foundDevices.clear();
        deviceList.removeAllViews();
        statusText.setText("正在扫描 BLE 设备...");

        if (isScanning) return;
        isScanning = true;

        try {
            leScanner.startScan(scanCallback);
        } catch (SecurityException e) {
            statusText.setText("⚠️ 缺少蓝牙权限，使用模拟模式");
            loadMockDevices();
            return;
        }

        // 5 秒后停止扫描
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isScanning) {
                try { leScanner.stopScan(scanCallback); } catch (SecurityException ignored) {}
                isScanning = false;
            }
            if (foundDevices.isEmpty()) {
                statusText.setText("未发现 BLE 设备，自动加载模拟设备");
                loadMockDevices();
            } else {
                statusText.setText("扫描完成，发现 " + foundDevices.size() + " 个设备");
            }
        }, 5000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                initBluetooth();
            } else {
                statusText.setText("⚠️ 蓝牙未开启，使用模拟模式");
                loadMockDevices();
            }
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            if (name == null) return;

            // 过滤健康相关设备
            boolean isHealthDevice = name.contains("BP") || name.contains("OX") || name.contains("HR")
                || name.contains("血压") || name.contains("血氧") || name.contains("心率")
                || name.contains("Band") || name.contains("Watch");

            if (!isHealthDevice) return;

            BluetoothDeviceMock mock = new BluetoothDeviceMock(
                foundDevices.size() + 1, name,
                name.contains("BP") || name.contains("血压") ? "blood_pressure" :
                    name.contains("OX") || name.contains("血氧") ? "blood_oxygen" : "heart_rate",
                name.contains("BP") ? "128/82" : name.contains("OX") ? "97" : "72",
                "已发现"
            );

            // 去重
            boolean duplicate = false;
            for (BluetoothDeviceMock existing : foundDevices) {
                if (existing.name.equals(mock.name)) { duplicate = true; break; }
            }
            if (!duplicate) {
                foundDevices.add(mock);
                addDeviceRow(mock);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            statusText.setText("⚠️ BLE 扫描失败（错误码 " + errorCode + "），使用模拟模式");
            loadMockDevices();
        }
    };

    private void loadMockDevices() {
        foundDevices.clear();
        deviceList.removeAllViews();
        foundDevices.addAll(MockData.getBluetoothDevices());
        for (BluetoothDeviceMock device : foundDevices) {
            addDeviceRow(device);
        }
        statusText.setText("已加载 " + foundDevices.size() + " 个模拟设备");
    }

    private void addDeviceRow(BluetoothDeviceMock device) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), dp(16), dp(18), dp(16));
        row.setBackgroundColor(getColor(R.color.surface_white));
        row.setBackgroundResource(R.drawable.bg_card_surface);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = new TextView(this);
        nameView.setText(device.name);
        nameView.setTextSize(18);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        nameView.setTextColor(getColor(R.color.text_primary));
        texts.addView(nameView);

        TextView metaView = new TextView(this);
        metaView.setText("类型：" + getTypeLabel(device.type) + " · 读数：" + device.value + " · " + device.status);
        metaView.setTextSize(14);
        metaView.setTextColor(getColor(R.color.text_secondary));
        metaView.setPadding(0, dp(4), 0, 0);
        texts.addView(metaView);

        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        Button connectBtn = new Button(this);
        connectBtn.setText("连接");
        connectBtn.setTextSize(14);
        connectBtn.setAllCaps(false);
        connectBtn.setBackgroundResource(R.drawable.bg_chip_soft);
        connectBtn.setTextColor(getColor(R.color.primary));
        connectBtn.setOnClickListener(v -> connectDevice(device));
        row.addView(connectBtn);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        row.setLayoutParams(params);
        deviceList.addView(row);
    }

    private void connectDevice(BluetoothDeviceMock device) {
        MockData.addHealthData(device.type, device.value, "正常");
        device.status = "已连接";
        new AlertDialog.Builder(this)
            .setTitle("✅ 连接成功")
            .setMessage(device.name + "\n已读取数据：" + device.value + "\n数据已写入健康档案。\n\n现在可以在健康探索中查看。")
            .setPositiveButton("完成", (d, w) -> {
                // 刷新设备列表
                deviceList.removeAllViews();
                for (BluetoothDeviceMock dev : foundDevices) {
                    addDeviceRow(dev);
                }
            })
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BT_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                statusText.setText("⚠️ 蓝牙权限被拒绝，使用模拟模式");
                Toast.makeText(this, "需要蓝牙权限才能扫描设备，请在设置中授权", Toast.LENGTH_LONG).show();
                loadMockDevices();
            }
        }
    }

    private String getTypeLabel(String type) {
        switch (type) {
            case "blood_pressure": return "血压";
            case "blood_oxygen": return "血氧";
            case "heart_rate": return "心率";
            default: return type;
        }
    }

    private Button btn(String text, int bgRes, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setAllCaps(false);
        button.setBackgroundResource(bgRes);
        button.setTextColor(textColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.bottomMargin = dp(10);
        button.setLayoutParams(params);
        return button;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
