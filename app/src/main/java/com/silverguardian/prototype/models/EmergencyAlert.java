package com.silverguardian.prototype.models;

public class EmergencyAlert {
    public int id;
    public String time;
    public String message;
    public String status;

    public EmergencyAlert(int id, String time, String message, String status) {
        this.id = id;
        this.time = time;
        this.message = message;
        this.status = status;
    }
}
