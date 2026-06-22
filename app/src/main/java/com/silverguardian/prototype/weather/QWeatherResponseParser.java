package com.silverguardian.prototype.weather;

import org.json.JSONArray;
import org.json.JSONObject;

// 和风天气响应解析器：JSON解析为WeatherReport对象
public final class QWeatherResponseParser {
    private QWeatherResponseParser() { }

    // 解析天气和空气质量JSON响应，提取天气类型、温度、AQI
    public static WeatherConditions parse(String weatherJson, String airJson) {
        String weatherType = "\u5929\u6c14\u6682\u4e0d\u53ef\u7528";
        Integer temperature = null;
        Integer aqi = null;
        String category = "";
        try {
            JSONObject weatherRoot = new JSONObject(weatherJson == null ? "{}" : weatherJson);
            JSONObject now = weatherRoot.optJSONObject("now");
            if (now != null) {
                weatherType = now.optString("text", weatherType);
                temperature = integer(now.optString("temp", null));
            }
        } catch (Exception ignored) { }
        try {
            JSONObject airRoot = new JSONObject(airJson == null ? "{}" : airJson);
            JSONArray indexes = airRoot.optJSONArray("indexes");
            JSONObject index = indexes == null || indexes.length() == 0 ? null : indexes.optJSONObject(0);
            if (index != null) {
                aqi = integer(index.optString("aqi", null));
                category = index.optString("category", "");
            }
        } catch (Exception ignored) { }
        return new WeatherConditions(weatherType, temperature, aqi, category);
    }

    private static Integer integer(String value) {
        try { return value == null || value.trim().isEmpty() ? null : Integer.valueOf(value.trim()); }
        catch (NumberFormatException ignored) { return null; }
    }
}