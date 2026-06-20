package com.silverguardian.prototype.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeatherAdviceEngine {
    private static final String[] UNSAFE_WEATHER = {"雨", "雪", "大风", "台风", "强风", "暴风"};

    private WeatherAdviceEngine() { }

    public static Decision evaluate(WeatherConditions conditions) {
        List<String> reasons = new ArrayList<>();
        for (String token : UNSAFE_WEATHER) {
            if (conditions.weatherType.contains(token)) {
                reasons.add("天气不适合外出");
                break;
            }
        }
        if (conditions.temperatureC == null) reasons.add("温度暂不可用");
        else if (conditions.temperatureC > 35 || conditions.temperatureC < 5) reasons.add("温度不适合外出");
        if (conditions.aqi == null) reasons.add("空气质量暂不可用");
        else if (conditions.aqi > 150) reasons.add("空气质量不适合外出");
        return new Decision(reasons.isEmpty(), reasons);
    }

    public static final class Decision {
        public final boolean suitableForOuting;
        public final List<String> reasons;
        Decision(boolean suitableForOuting, List<String> reasons) {
            this.suitableForOuting = suitableForOuting;
            this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
        }
    }
}