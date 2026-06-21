package com.silverguardian.prototype.health;

public final class EvaluationResult {
    public final String displayLabel;
    public final AlertLevel alertLevel;

    public EvaluationResult(String displayLabel, AlertLevel alertLevel) {
        this.displayLabel = displayLabel;
        this.alertLevel = alertLevel;
    }
}