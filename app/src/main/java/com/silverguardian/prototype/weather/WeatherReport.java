package com.silverguardian.prototype.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeatherReport {
    public final String summary;
    public final String advice;
    public final String notificationSummary;
    public final boolean suitableForOuting;
    public final List<WeatherNavigationPlace> recommendedPlaces;

    public WeatherReport(String summary, String advice, String notificationSummary, boolean suitableForOuting,
                         List<WeatherNavigationPlace> recommendedPlaces) {
        this.summary = summary;
        this.advice = advice;
        this.notificationSummary = notificationSummary;
        this.suitableForOuting = suitableForOuting;
        this.recommendedPlaces = recommendedPlaces == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(recommendedPlaces));
    }
}
