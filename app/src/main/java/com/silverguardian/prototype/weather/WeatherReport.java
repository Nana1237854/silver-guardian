package com.silverguardian.prototype.weather;

public final class WeatherReport {
    public final String summary;
    public final String advice;
    public final String notificationSummary;
    public final boolean suitableForOuting;

    public WeatherReport(String summary, String advice, String notificationSummary, boolean suitableForOuting) {
        this.summary = summary;
        this.advice = advice;
        this.notificationSummary = notificationSummary;
        this.suitableForOuting = suitableForOuting;
    }
}