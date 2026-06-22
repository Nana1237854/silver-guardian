package com.silverguardian.prototype.weather;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

// 天气模块总入口：定位获取、天气数据加载、出行POI推荐
public final class WeatherModule {
    public interface Callback { void onResult(WeatherReport report); }

    private static final LatLonPoint DEFAULT_GUANGZHOU = new LatLonPoint(23.1291, 113.2644);
    private static final String PREFS = "weather_location";
    private static final String[] LEISURE_KEYWORDS = {"公园", "广场", "社区活动中心"};

    private final Context appContext;
    private final WeatherClient weatherClient;

    public WeatherModule(Context context) {
        appContext = context.getApplicationContext();
        weatherClient = new WeatherClient();
        AMapLocationClient.updatePrivacyShow(appContext, true, true);
        AMapLocationClient.updatePrivacyAgree(appContext, true);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void load(Callback callback) {
        locate((point, cityHint, districtHint) -> loadAt(point, cityHint, districtHint, callback));
    }

    public void loadFromSavedLocation(Callback callback) {
        loadAt(savedPoint(), "广州", "", callback);
    }

    private void loadAt(LatLonPoint point, String cityHint, String districtHint, Callback callback) {
        AtomicBoolean delivered = new AtomicBoolean(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (delivered.compareAndSet(false, true)) callback.onResult(unavailableReport(joinPlace(cityHint, districtHint)));
        }, 15000);
        reverseGeocode(point, cityHint, districtHint,
            place -> weatherClient.load(point.getLatitude(), point.getLongitude(), conditions -> {
                WeatherAdviceEngine.Decision decision = WeatherAdviceEngine.evaluate(conditions);
                loadPlaces(point, (leisure, hospital) -> {
                    WeatherReport report = buildReport(place, conditions, decision, leisure, hospital);
                    if (delivered.compareAndSet(false, true)) callback.onResult(report);
                });
            }));
    }

    private static WeatherReport unavailableReport(String place) {
        String summary = place + " 天气与空气质量暂不可用";
        String advice = "暂不建议安排外出；如有不适请联系家属或就医。";
        return new WeatherReport(summary, advice, summary + "；" + advice, false);
    }

    private interface LocationCallback { void done(LatLonPoint point, String city, String district); }
    private interface PlaceCallback { void done(String place); }
    private interface PoiCallback { void done(List<PoiItem> pois); }
    private interface PlacesCallback { void done(List<PoiItem> leisure, PoiItem hospital); }

    private void locate(LocationCallback callback) {
        if (!hasLocationPermission()) {
            LatLonPoint saved = savedPoint();
            callback.done(saved, "广州", "");
            return;
        }
        AtomicBoolean completed = new AtomicBoolean(false);
        try {
            AMapLocationClient client = new AMapLocationClient(appContext);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            option.setOnceLocationLatest(true);
            option.setHttpTimeOut(8000);
            client.setLocationOption(option);
            client.setLocationListener(location -> {
                if (!completed.compareAndSet(false, true)) return;
                client.stopLocation();
                client.onDestroy();
                if (location != null && location.getErrorCode() == 0) {
                    LatLonPoint point = new LatLonPoint(location.getLatitude(), location.getLongitude());
                    savePoint(point);
                    callback.done(point, safe(location.getCity()), safe(location.getDistrict()));
                } else callback.done(savedPoint(), "广州", "");
            });
            client.startLocation();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!completed.compareAndSet(false, true)) return;
                client.stopLocation();
                client.onDestroy();
                callback.done(savedPoint(), "广州", "");
            }, 9000);
        } catch (Exception ignored) {
            callback.done(savedPoint(), "广州", "");
        }
    }

    private void reverseGeocode(LatLonPoint point, String cityHint, String districtHint, PlaceCallback callback) {
        try {
            GeocodeSearch search = new GeocodeSearch(appContext);
            search.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                @Override public void onGeocodeSearched(com.amap.api.services.geocoder.GeocodeResult result, int code) { }
                @Override public void onRegeocodeSearched(RegeocodeResult result, int code) {
                    if (code == 1000 && result != null && result.getRegeocodeAddress() != null) {
                        RegeocodeAddress address = result.getRegeocodeAddress();
                        String city = safe(address.getCity());
                        if (city.isEmpty()) city = safe(address.getProvince());
                        callback.done(joinPlace(city, safe(address.getDistrict())));
                    } else callback.done(joinPlace(cityHint, districtHint));
                }
            });
            search.getFromLocationAsyn(new RegeocodeQuery(point, 200, GeocodeSearch.AMAP));
        } catch (Exception ignored) {
            callback.done(joinPlace(cityHint, districtHint));
        }
    }

    private void loadPlaces(LatLonPoint point, PlacesCallback callback) {
        PoiBundle bundle = new PoiBundle(callback);
        searchLeisure(point, 0, bundle::setLeisure);
        searchPoi(point, "医院", 1, pois -> bundle.setHospital(pois.isEmpty() ? null : pois.get(0)));
    }

    private void searchLeisure(LatLonPoint point, int keywordIndex, PoiCallback callback) {
        if (keywordIndex >= LEISURE_KEYWORDS.length) { callback.done(Collections.emptyList()); return; }
        searchPoi(point, LEISURE_KEYWORDS[keywordIndex], 3, pois -> {
            if (pois.isEmpty()) searchLeisure(point, keywordIndex + 1, callback);
            else callback.done(pois);
        });
    }

    private void searchPoi(LatLonPoint point, String keyword, int limit, PoiCallback callback) {
        try {
            PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
            query.setPageSize(Math.max(limit, 10));
            query.setPageNum(0);
            PoiSearch search = new PoiSearch(appContext, query);
            search.setBound(new PoiSearch.SearchBound(point, 5000));
            search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override public void onPoiItemSearched(PoiItem item, int code) { }
                @Override public void onPoiSearched(PoiResult result, int code) {
                    if (code != 1000 || result == null || result.getPois() == null) { callback.done(Collections.emptyList()); return; }
                    List<PoiItem> sorted = new ArrayList<>(result.getPois());
                    sorted.sort(Comparator.comparingInt(PoiItem::getDistance));
                    callback.done(new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size()))));
                }
            });
            search.searchPOIAsyn();
        } catch (Exception ignored) { callback.done(Collections.emptyList()); }
    }

    private WeatherReport buildReport(String place, WeatherConditions conditions, WeatherAdviceEngine.Decision decision,
                                      List<PoiItem> leisure, PoiItem hospital) {
        String temperature = conditions.temperatureC == null ? "温度暂不可用" : conditions.temperatureC + "°C";
        String air = conditions.aqi == null ? "空气质量暂不可用" : "空气" + conditions.airCategory + " AQI " + conditions.aqi;
        String summary = place + " " + conditions.weatherType + " " + temperature + " | " + air;
        StringBuilder advice = new StringBuilder();
        boolean weatherAvailable = conditions.temperatureC != null && conditions.aqi != null && !conditions.weatherType.contains("暂不可用");
        if (!weatherAvailable) advice.append("天气或空气质量数据暂不可用，暂不建议安排外出。");
        else if (decision.suitableForOuting) {
            advice.append("今天适合出门。");
            if (!leisure.isEmpty()) {
                advice.append("推荐：");
                for (int i = 0; i < leisure.size(); i++) {
                    if (i > 0) advice.append("、");
                    advice.append(leisure.get(i).getTitle()).append("，距您").append(distance(leisure.get(i)));
                }
                advice.append("。");
            }
        } else advice.append("建议在家活动。");
        if (hospital != null) advice.append("如需就医，").append(hospital.getTitle()).append("距您").append(distance(hospital)).append("。");
        String notification = summary + "；" + advice;
        return new WeatherReport(summary, advice.toString(), notification, decision.suitableForOuting && weatherAvailable);
    }

    private static String distance(PoiItem poi) {
        int meters = poi.getDistance();
        if (meters <= 0) return "未知";
        return meters < 1000 ? meters + "m" : String.format(Locale.CHINA, "%.1fkm", meters / 1000f);
    }

    private LatLonPoint savedPoint() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        double lat = Double.longBitsToDouble(prefs.getLong("lat", Double.doubleToRawLongBits(DEFAULT_GUANGZHOU.getLatitude())));
        double lon = Double.longBitsToDouble(prefs.getLong("lon", Double.doubleToRawLongBits(DEFAULT_GUANGZHOU.getLongitude())));
        return new LatLonPoint(lat, lon);
    }

    private void savePoint(LatLonPoint point) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("lat", Double.doubleToRawLongBits(point.getLatitude()))
            .putLong("lon", Double.doubleToRawLongBits(point.getLongitude())).apply();
    }

    private static String joinPlace(String city, String district) {
        String value = safe(city) + (safe(district).equals(safe(city)) ? "" : safe(district));
        return value.isEmpty() ? "当前位置" : value;
    }
    private static String safe(String value) { return value == null ? "" : value.trim(); }

    private static final class PoiBundle {
        private final PlacesCallback callback;
        private boolean leisureDone;
        private boolean hospitalDone;
        private List<PoiItem> leisure = Collections.emptyList();
        private PoiItem hospital;
        PoiBundle(PlacesCallback callback) { this.callback = callback; }
        synchronized void setLeisure(List<PoiItem> value) { leisure = value; leisureDone = true; finish(); }
        synchronized void setHospital(PoiItem value) { hospital = value; hospitalDone = true; finish(); }
        private void finish() { if (leisureDone && hospitalDone) callback.done(leisure, hospital); }
    }
}