package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.SafeCheckRecord;

// 每日平安确认业务模块：生成平安确认请求、处理老人确认/否认
public class SafeCheckModule {
    private final Repository repository;

    public SafeCheckModule(Repository repository) {
        this.repository = repository;
    }

    public SafeCheckRecord getTodayRecord() {
        return repository.getTodaySafeCheckRecord();
    }

    public SafeCheckRecord saveTodayStatus(String status) {
        return repository.saveTodaySafeCheckStatus(status, "");
    }

    public SafeCheckRecord saveTodayStatus(String status, String note) {
        return repository.saveTodaySafeCheckStatus(status, note);
    }

    public boolean hasConfirmedToday() {
        SafeCheckRecord record = getTodayRecord();
        return record != null && record.isConfirmed();
    }
}
