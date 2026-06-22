package com.silverguardian.prototype.health;

import com.silverguardian.prototype.models.HealthData;

// 健康指标阈值判断引擎：返回正常/警告/严重三级评估
public final class HealthMetricEvaluator {
    private HealthMetricEvaluator() { }

    // 根据指标类型和数值执行阈值判断，返回正常/警告/严重评估结果
    public static EvaluationResult evaluate(String type, String value) {
        try {
            switch (type == null ? "" : type) {
                case "heart_rate": {
                    int heartRate = parseInt(value);
                    if (heartRate < 45) return result("偏低", AlertLevel.CRITICAL);
                    if (heartRate < 55) return result("偏低", AlertLevel.WARNING);
                    if (heartRate > 130) return result("偏高", AlertLevel.CRITICAL);
                    if (heartRate > 110) return result("偏高", AlertLevel.WARNING);
                    return normal();
                }
                case "blood_pressure": {
                    String[] parts = value == null ? new String[0] : value.split("/");
                    if (parts.length < 2) return needsAttention();
                    int systolic = parseInt(parts[0]);
                    int diastolic = parseInt(parts[1]);
                    if (systolic > 180 || diastolic > 120) return result("偏高", AlertLevel.CRITICAL);
                    if (systolic > 160 || diastolic > 100) return result("偏高", AlertLevel.WARNING);
                    if (systolic < 80) return result("偏低", AlertLevel.WARNING);
                    return normal();
                }
                case "blood_oxygen": {
                    int bloodOxygen = parseInt(value);
                    if (bloodOxygen < 90) return result("偏低", AlertLevel.CRITICAL);
                    if (bloodOxygen < 94) return result("偏低", AlertLevel.WARNING);
                    return normal();
                }
                case "temperature": {
                    double temperature = parseDouble(value);
                    if (temperature < 35.0 || temperature > 39.0) return result("异常", AlertLevel.CRITICAL);
                    if (temperature < 35.5) return result("偏低", AlertLevel.WARNING);
                    if (temperature > 38.0) return result("偏高", AlertLevel.WARNING);
                    return normal();
                }
                case "blood_sugar": {
                    double bloodSugar = parseDouble(value);
                    if (bloodSugar < 3.0) return result("偏低", AlertLevel.CRITICAL);
                    if (bloodSugar < 4.0) return result("偏低", AlertLevel.WARNING);
                    if (bloodSugar > 16.0) return result("偏高", AlertLevel.CRITICAL);
                    if (bloodSugar > 11.0) return result("偏高", AlertLevel.WARNING);
                    return normal();
                }
                case "respiratory_rate": {
                    int respiratoryRate = parseInt(value);
                    if (respiratoryRate < 8 || respiratoryRate > 30) return result("异常", AlertLevel.CRITICAL);
                    if (respiratoryRate < 10) return result("偏低", AlertLevel.WARNING);
                    if (respiratoryRate > 24) return result("偏高", AlertLevel.WARNING);
                    return normal();
                }
                default:
                    return normal();
            }
        } catch (RuntimeException e) {
            return needsAttention();
        }
    }

    public static String status(String type, String value) {
        return evaluate(type, value).displayLabel;
    }

    public static boolean isAbnormal(HealthData data) {
        return !"正常".equals(data.status) && !"良好".equals(data.status);
    }

    private static int parseInt(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9]", "");
        return Integer.parseInt(digits);
    }

    private static double parseDouble(String value) {
        String digits = value == null ? "" : value.replaceAll("[^0-9.]", "");
        return Double.parseDouble(digits);
    }

    private static EvaluationResult normal() {
        return result("正常", AlertLevel.NORMAL);
    }

    private static EvaluationResult needsAttention() {
        return result("需关注", AlertLevel.NORMAL);
    }

    private static EvaluationResult result(String displayLabel, AlertLevel alertLevel) {
        return new EvaluationResult(displayLabel, alertLevel);
    }
}