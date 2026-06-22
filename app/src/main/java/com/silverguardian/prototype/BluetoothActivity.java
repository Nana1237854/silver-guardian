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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.models.BluetoothDeviceMock;

import java.util.ArrayList;
import java.util.List;

// 蓝牙设备页面：BLE扫描、设备连接、演示设备降级方案
public class BluetoothActivity extends BaseActivity {
    private static final int REQUEST_BT_PERMISSIONS = 301;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String TYPE_BLOOD_PRESSURE = "blood_pressure";
    private static final String TYPE_BLOOD_OXYGEN = "blood_oxygen";
    private static final String TYPE_HEART_RATE = "heart_rate";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner leScanner;
    private RecyclerView deviceList;
    private TextView statusText;
    private TextView emptyState;
    private DeviceAdapter adapter;
    private boolean isScanning = false;
    private final List<BluetoothDeviceMock> foundDevices = new ArrayList<>();

    @Override
    // 初始化蓝牙适配器与扫描器，绑定页面控件
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
setContentView(R.layout.activity_bluetooth);
        bindHeader();
        bindControls();
        bindList();
        statusText.setText(R.string.bluetooth_initializing);
        refreshDevices();
        initBluetooth();
    }

    private void bindHeader() {
        View header = findViewById(R.id.bluetooth_header);
        TextView back = header.findViewById(R.id.header_back);
        back.setVisibility(View.VISIBLE);
        back.setText(R.string.common_back);
        back.setOnClickListener(v -> finish());
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.tab_bluetooth);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindControls() {
        statusText = findViewById(R.id.bluetooth_status);
        emptyState = findViewById(R.id.bluetooth_empty_state);
        emptyState.setText(R.string.bluetooth_empty_state);

        TextView intro = findViewById(R.id.bluetooth_intro);
        intro.setText(R.string.bluetooth_intro);

        TextView scanButton = findViewById(R.id.bluetooth_scan_button);
        scanButton.setText(R.string.bluetooth_scan_button);
        scanButton.setOnClickListener(v -> startScan());

        TextView mockButton = findViewById(R.id.bluetooth_mock_button);
        mockButton.setText(R.string.bluetooth_mock_button);
        mockButton.setOnClickListener(v -> loadMockDevices());
    }

    private void bindList() {
        deviceList = findViewById(R.id.bluetooth_device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter();
        deviceList.setAdapter(adapter);
    }

    private void refreshDevices() {
        if (emptyState != null) {
            emptyState.setVisibility(foundDevices.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            statusText.setText(R.string.bluetooth_not_supported_ble);
            loadMockDevices();
            return;
        }

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (btManager == null) {
            statusText.setText(R.string.bluetooth_service_fail);
            loadMockDevices();
            return;
        }

        bluetoothAdapter = btManager.getAdapter();
        if (bluetoothAdapter == null) {
            statusText.setText(R.string.bluetooth_unsupported);
            loadMockDevices();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            statusText.setText(R.string.bluetooth_enable_first);
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
            return;
        }

        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (leScanner == null) {
            statusText.setText(R.string.bluetooth_scanner_fail);
            loadMockDevices();
            return;
        }

        statusText.setText(R.string.bluetooth_ready);
    }

    // 检查权限后开始BLE设备扫描，5秒超时后回退演示设备
    private void startScan() {
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
            Toast.makeText(this, R.string.bluetooth_scanner_unavailable, Toast.LENGTH_SHORT).show();
            loadMockDevices();
            return;
        }

        if (isScanning) {
            return;
        }

        foundDevices.clear();
        refreshDevices();
        statusText.setText(R.string.bluetooth_scanning);
        isScanning = true;

        try {
            leScanner.startScan(scanCallback);
        } catch (SecurityException e) {
            statusText.setText(R.string.bluetooth_permission_fallback);
            isScanning = false;
            loadMockDevices();
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isScanning) {
                try {
                    leScanner.stopScan(scanCallback);
                } catch (SecurityException ignored) {
                }
                isScanning = false;
            }
            if (foundDevices.isEmpty()) {
                statusText.setText(R.string.bluetooth_scan_empty);
                loadMockDevices();
            } else {
                statusText.setText(getString(R.string.bluetooth_scan_done, foundDevices.size()));
                refreshDevices();
            }
        }, 5000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                initBluetooth();
            } else {
                statusText.setText(R.string.bluetooth_disabled);
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
            if (name == null || name.trim().isEmpty()) {
                return;
            }
            if (!isHealthDevice(name)) {
                return;
            }

            BluetoothDeviceMock mock = new BluetoothDeviceMock(
                foundDevices.size() + 1,
                name,
                detectType(name),
                detectValue(name),
                getString(R.string.bluetooth_status_found)
            );

            if (isDuplicate(mock)) {
                return;
            }
            foundDevices.add(mock);
            refreshDevices();
        }

        @Override
        public void onScanFailed(int errorCode) {
            isScanning = false;
            statusText.setText(getString(R.string.bluetooth_scan_failed, errorCode));
            loadMockDevices();
        }
    };

    private boolean isHealthDevice(String name) {
        String bloodPressure = getString(R.string.bluetooth_type_blood_pressure);
        String bloodOxygen = getString(R.string.bluetooth_type_blood_oxygen);
        String heartRate = getString(R.string.bluetooth_type_heart_rate);
        return name.contains("BP")
            || name.contains("OX")
            || name.contains("HR")
            || name.contains(bloodPressure)
            || name.contains(bloodOxygen)
            || name.contains(heartRate)
            || name.contains("Band")
            || name.contains("Watch");
    }

    private String detectType(String name) {
        if (name.contains("BP") || name.contains(getString(R.string.bluetooth_type_blood_pressure))) {
            return TYPE_BLOOD_PRESSURE;
        }
        if (name.contains("OX") || name.contains(getString(R.string.bluetooth_type_blood_oxygen))) {
            return TYPE_BLOOD_OXYGEN;
        }
        return TYPE_HEART_RATE;
    }

    private String detectValue(String name) {
        if (name.contains("BP") || name.contains(getString(R.string.bluetooth_type_blood_pressure))) {
            return "128/82";
        }
        if (name.contains("OX") || name.contains(getString(R.string.bluetooth_type_blood_oxygen))) {
            return "97";
        }
        return "72";
    }

    private boolean isDuplicate(BluetoothDeviceMock mock) {
        for (BluetoothDeviceMock existing : foundDevices) {
            if (existing.name.equals(mock.name)) {
                return true;
            }
        }
        return false;
    }

    private void loadMockDevices() {
        foundDevices.clear();
        foundDevices.addAll(healthRecords().getBluetoothDevices());
        statusText.setText(getString(R.string.bluetooth_mock_loaded, foundDevices.size()));
        refreshDevices();
    }

    // 连接指定蓝牙健康设备并将数据同步至健康记录
    private void connectDevice(BluetoothDeviceMock device) {
        healthRecords().addHealthData(device.type, device.value, getString(R.string.bluetooth_health_status_normal));
        device.status = getString(R.string.common_connected);
        new AlertDialog.Builder(this)
            .setTitle(R.string.bluetooth_connect_success_title)
            .setMessage(getString(R.string.bluetooth_connect_success_message, device.name, device.value))
            .setPositiveButton(R.string.common_save, (dialog, which) -> refreshDevices())
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BT_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                statusText.setText(R.string.bluetooth_permission_denied);
                Toast.makeText(this, R.string.bluetooth_permission_toast, Toast.LENGTH_LONG).show();
                loadMockDevices();
            }
        }
    }

    private String getTypeLabel(String type) {
        switch (type) {
            case TYPE_BLOOD_PRESSURE:
                return getString(R.string.bluetooth_type_blood_pressure);
            case TYPE_BLOOD_OXYGEN:
                return getString(R.string.bluetooth_type_blood_oxygen);
            case TYPE_HEART_RATE:
                return getString(R.string.bluetooth_type_heart_rate);
            default:
                return type;
        }
    }

    private class DeviceAdapter extends RecyclerView.Adapter<DeviceHolder> {
        @NonNull
        @Override
        public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
            return new DeviceHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
            holder.bind(foundDevices.get(position));
        }

        @Override
        public int getItemCount() {
            return foundDevices.size();
        }
    }

    private class DeviceHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView type;
        private final TextView meta;
        private final TextView connect;

        DeviceHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.bluetooth_device_name);
            type = itemView.findViewById(R.id.bluetooth_device_type);
            meta = itemView.findViewById(R.id.bluetooth_device_meta);
            connect = itemView.findViewById(R.id.bluetooth_device_connect);
        }

        void bind(BluetoothDeviceMock device) {
            String typeLabel = getTypeLabel(device.type);
            String typeText = getString(R.string.bluetooth_type_label, typeLabel);
            String valueText = getString(R.string.bluetooth_value_label, device.value);
            String statusText = getString(R.string.bluetooth_status_label, device.status);
            name.setText(device.name);
            type.setText(typeLabel);
            meta.setText(getString(R.string.bluetooth_device_meta, typeText, valueText, statusText));
            boolean connected = getString(R.string.common_connected).equals(device.status);
            connect.setText(connected ? R.string.common_connected : R.string.common_connect);
            connect.setAlpha(connected ? 0.65f : 1f);
            connect.setOnClickListener(v -> {
                if (!connected) {
                    connectDevice(device);
                }
            });
            itemView.setOnClickListener(v -> {
                if (!connected) {
                    connectDevice(device);
                }
            });
        }
    }
}
