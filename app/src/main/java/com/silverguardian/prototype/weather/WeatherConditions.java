package com.silverguardian.prototype.weather;

public final class WeatherConditions {
    public final String weatherType;
    public final Integer temperatureC;
    public final Integer aqi;
    public final String airCategory;

    public WeatherConditions(String weatherType, Integer temperatureC, Integer aqi, String airCategory) {
        this.weatherType = weatherType == null ? "" : weatherType;
        this.temperatureC = temperatureC;
        this.aqi = aqi;
        this.airCategory = airCategory == null ? "" : airCategory;
    }
}