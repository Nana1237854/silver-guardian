package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.BluetoothDeviceMock;
import com.silverguardian.prototype.models.HealthData;

import java.util.ArrayList;
import java.util.List;

public class HealthRecordModule {
    private final Repository repository;

    public HealthRecordModule(Repository repository) {
        this.repository = repository;
    }

    public List<HealthData> getHealthData() {
        return new ArrayList<>(repository.getHealthData());
    }

    public HealthData addHealthData(String type, String value, String status) {
        return repository.addHealthData(type, value, status);
    }

    public List<BluetoothDeviceMock> getBluetoothDevices() {
        return new ArrayList<>(repository.getBluetoothDevices());
    }
}
