package com.silverguardian.prototype.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 天气结果数据模型：首页天气卡片展示数据
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
