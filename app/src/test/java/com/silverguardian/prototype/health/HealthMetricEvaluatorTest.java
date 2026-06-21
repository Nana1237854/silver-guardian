package com.silverguardian.prototype.health;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HealthMetricEvaluatorTest {
    @Test
    public void evaluatesCoreMetricsWithDisplayLabelAndAlertLevel() {
        EvaluationResult highHeartRate = HealthMetricEvaluator.evaluate("heart_rate", "130");
        assertEquals("偏高", highHeartRate.displayLabel);
        assertEquals(AlertLevel.WARNING, highHeartRate.alertLevel);

        EvaluationResult lowBloodOxygen = HealthMetricEvaluator.evaluate("blood_oxygen", "90");
        assertEquals("偏低", lowBloodOxygen.displayLabel);
        assertEquals(AlertLevel.WARNING, lowBloodOxygen.alertLevel);

        EvaluationResult normalBloodPressure = HealthMetricEvaluator.evaluate("blood_pressure", "128/76");
        assertEquals("正常", normalBloodPressure.displayLabel);
        assertEquals(AlertLevel.NORMAL, normalBloodPressure.alertLevel);
    }

    @Test
    public void distinguishesCriticalValuesAndKeepsStatusCompatibility() {
        EvaluationResult critical = HealthMetricEvaluator.evaluate("heart_rate", "140");
        assertEquals("偏高", critical.displayLabel);
        assertEquals(AlertLevel.CRITICAL, critical.alertLevel);
        assertEquals("偏高", HealthMetricEvaluator.status("heart_rate", "140"));
    }

    @Test
    public void malformedValueNeedsAttentionWithoutAutomaticAlert() {
        EvaluationResult malformed = HealthMetricEvaluator.evaluate("blood_pressure", "unknown");
        assertEquals("需关注", malformed.displayLabel);
        assertEquals(AlertLevel.NORMAL, malformed.alertLevel);
    }

    @Test
    public void unknownMetricDefaultsNormal() {
        assertEquals("正常", HealthMetricEvaluator.status("mood", "良好"));
    }
}