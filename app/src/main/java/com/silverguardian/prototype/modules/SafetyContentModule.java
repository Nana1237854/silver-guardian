package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FraudTip;

import java.util.ArrayList;
import java.util.List;

// 本地预置防诈内容兜底：网络不可用时提供内置防诈知识
public class SafetyContentModule {
    private final Repository repository;

    public SafetyContentModule(Repository repository) {
        this.repository = repository;
    }

    public List<FraudTip> getFraudTips() {
        return new ArrayList<>(repository.getFraudTips());
    }
}
