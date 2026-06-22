package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.MedicineFeedback;

import java.util.ArrayList;
import java.util.List;

// 服药后身体反馈业务模块：记录老人服药后感受
public class MedicineFeedbackModule {
    private final Repository repository;

    public MedicineFeedbackModule(Repository repository) {
        this.repository = repository;
    }

    public MedicineFeedback addFeedback(int medicineId, String medicineName, String feedbackType, String feedbackText) {
        return repository.addMedicineFeedback(medicineId, medicineName, feedbackType, feedbackText);
    }

    public List<MedicineFeedback> getTodayFeedbacks() {
        return new ArrayList<>(repository.getTodayMedicineFeedbacks());
    }

    public int getTodayWarningCount() {
        return repository.getTodayMedicineFeedbackWarningCount();
    }

    public List<MedicineFeedback> getTodayWarnings() {
        return new ArrayList<>(repository.getTodayMedicineFeedbackWarnings());
    }
}
