package com.silverguardian.prototype.health;

// 健康评估结果模型：评估等级、异常指标、建议信息
public final class EvaluationResult {
    public final String displayLabel;
    public final AlertLevel alertLevel;

    public EvaluationResult(String displayLabel, AlertLevel alertLevel) {
        this.displayLabel = displayLabel;
        this.alertLevel = alertLevel;
    }
}