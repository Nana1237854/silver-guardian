package com.silverguardian.prototype.models;

public class BluetoothDeviceMock {
    public int id;
    public String name;
    public String type;
    public String value;
    public String status;

    public BluetoothDeviceMock(int id, String name, String type, String value, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
        this.status = status;
    }
}
