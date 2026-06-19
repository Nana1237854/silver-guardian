package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.EmergencyAlert;

import java.util.ArrayList;
import java.util.List;

public class EmergencyModule {
    private final Repository repository;

    public EmergencyModule(Repository repository) {
        this.repository = repository;
    }

    public List<EmergencyAlert> getAlerts() {
        return new ArrayList<>(repository.getEmergencyAlerts());
    }

    public EmergencyAlert addAlert(String message) {
        return repository.addEmergencyAlert(message);
    }
}
