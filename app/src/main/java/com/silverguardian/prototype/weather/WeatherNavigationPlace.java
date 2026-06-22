package com.silverguardian.prototype.weather;

import androidx.annotation.Nullable;

import com.amap.api.services.core.PoiItem;

// 首页一键导航推荐地点模型：POI名称、地址、距离
public final class WeatherNavigationPlace {
    public final String name;
    public final double lat;
    public final double lon;
    public final String type;
    public final String distanceText;
    @Nullable public final PoiItem poiItem;

    public WeatherNavigationPlace(String name, double lat, double lon, String type, String distanceText, @Nullable PoiItem poiItem) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.distanceText = distanceText;
        this.poiItem = poiItem;
    }
}
