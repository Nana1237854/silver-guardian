package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

public class SafetyContentModule {
    private final Repository repository;

    public SafetyContentModule(Repository repository) {
        this.repository = repository;
    }

    public List<FraudTip> getFraudTips() {
        return new ArrayList<>(repository.getFraudTips());
    }
}
