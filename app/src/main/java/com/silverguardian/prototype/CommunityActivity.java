package com.silverguardian.prototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 社区便民查询 — 高德 MapView + POI 搜索 + 步行路径规划。
 */
public class CommunityActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, RouteSearch.OnRouteSearchListener {

    private static final int REQUEST_LOCATION = 301;
    private MapView mapView;
    private AMap aMap;
    private LinearLayout searchBar;
    private PoiSearch poiSearch;
    private RouteSearch routeSearch;
    private TextView statusText;
    private List<PoiItem> currentPois = new ArrayList<>();

    // 广州商学院附近
    private final LatLng centerPoint = new LatLng(23.1291, 113.2644);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(R.color.bg_page));

        TextView pageTitle = new TextView(this);
        pageTitle.setText("便民查询");
        pageTitle.setTextSize(26);
        pageTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        pageTitle.setTextColor(getColor(R.color.text_primary));
        pageTitle.setPadding(dp(18), dp(16), dp(18), dp(4));
        root.addView(pageTitle);

        // 搜索按钮栏
        searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(dp(16), dp(12), dp(16), dp(8));
        btnRow.addView(searchChip("菜市场", "菜市场"));
        btnRow.addView(searchChip("社区医院", "社区卫生服务中心"));
        btnRow.addView(searchChip("药店", "药店"));
        btnRow.addView(searchChip("超市", "超市"));
        btnRow.addView(searchChip("银行", "银行"));
        hsv.addView(btnRow);
        root.addView(hsv);

        // 状态提示
        statusText = new TextView(this);
        statusText.setText("正在初始化地图...");
        statusText.setTextSize(14);
        statusText.setTextColor(getColor(R.color.text_secondary));
        statusText.setBackgroundResource(R.drawable.bg_reminder_strip);
        statusText.setPadding(dp(16), dp(10), dp(16), dp(10));
        root.addView(statusText);

        // 地图
        mapView = new MapView(this);
        root.addView(mapView, new LinearLayout.LayoutParams(-1, 0, 1));

        // 返回按钮
        TextView back = new TextView(this);
        back.setText("返回设置");
        back.setTextSize(18);
        back.setGravity(Gravity.CENTER);
        back.setTextColor(getColor(R.color.primary_dark));
        back.setBackgroundColor(getColor(R.color.surface_white));
        back.setPadding(0, dp(16), 0, dp(16));
        back.setOnClickListener(v -> finish());
        root.addView(back);

        setContentView(root);

        // 高德 SDK 隐私合规（必须在所有 API 调用前设置）
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);

        mapView.onCreate(savedInstanceState);
        initMap();
    }

    private void initMap() {
        aMap = mapView.getMap();
        if (aMap == null) {
            statusText.setText("地图初始化失败，请确认高德 API Key 已配置");
            return;
        }

        // 定位蓝点
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
        locationStyle.interval(2000);
        aMap.setMyLocationStyle(locationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);

        // 初始视角
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 15));

        // 初始化 POI 搜索
        try {
            poiSearch = new PoiSearch(this, null);
            poiSearch.setOnPoiSearchListener(this);
        } catch (Exception e) {
            statusText.setText("POI 搜索初始化失败: " + e.getMessage());
        }

        // 初始化路径规划
        try {
            routeSearch = new RouteSearch(this);
            routeSearch.setRouteSearchListener(this);
        } catch (Exception e) {
            // non-critical
        }

        statusText.setText("地图已就绪，选择上方类别搜索周边服务");

        // 检查定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        // 默认搜索菜市场
        searchPoi("菜市场");
    }

    private View searchChip(String label, String keyword) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextSize(15);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.setTextColor(getColor(R.color.primary));
        chip.setBackgroundResource(R.drawable.bg_chip_soft);
        chip.setPadding(dp(14), dp(10), dp(14), dp(10));
        chip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.rightMargin = dp(8);
        chip.setLayoutParams(params);
        chip.setOnClickListener(v -> searchPoi(keyword));
        return chip;
    }

    private void searchPoi(String keyword) {
        if (poiSearch == null) {
            Toast.makeText(this, "搜索服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        statusText.setText("正在搜索「" + keyword + "」...");
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "广州");
        query.setPageSize(20);
        query.setPageNum(0);
        PoiSearch.SearchBound bound = new PoiSearch.SearchBound(
            new LatLonPoint(centerPoint.latitude, centerPoint.longitude), 5000);
        poiSearch.setQuery(query);
        poiSearch.setBound(bound);
        poiSearch.searchPOIAsyn();
    }

    // ========== POI 搜索结果回调 ==========

    @Override
    public void onPoiSearched(PoiResult result, int errorCode) {
        if (errorCode != 1000 || result == null || result.getPois().isEmpty()) {
            statusText.setText("未找到结果（错误码 " + errorCode + "），请检查网络和高德 Key");
            return;
        }
        currentPois = result.getPois();
        aMap.clear();
        statusText.setText("找到 " + currentPois.size() + " 个周边结果");

        for (int i = 0; i < Math.min(currentPois.size(), 20); i++) {
            PoiItem poi = currentPois.get(i);
            LatLng point = new LatLng(poi.getLatLonPoint().getLatitude(), poi.getLatLonPoint().getLongitude());
            MarkerOptions marker = new MarkerOptions()
                .position(point)
                .title(poi.getTitle())
                .snippet(poi.getSnippet() + " · " + (poi.getDistance() > 0 ? poi.getDistance() + "m" : ""))
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(i)));
            aMap.addMarker(marker);
        }

        // 相机移到第一个POI
        if (!currentPois.isEmpty()) {
            PoiItem first = currentPois.get(0);
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(first.getLatLonPoint().getLatitude(), first.getLatLonPoint().getLongitude()), 15));
        }

        // 点击 Marker 规划步行路线
        aMap.setOnMarkerClickListener(marker -> {
            for (PoiItem poi : currentPois) {
                String markerPos = marker.getPosition().latitude + "," + marker.getPosition().longitude;
                String poiPos = poi.getLatLonPoint().getLatitude() + "," + poi.getLatLonPoint().getLongitude();
                if (markerPos.equals(poiPos)) {
                    showPoiDetail(poi);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {}

    private void showPoiDetail(PoiItem poi) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(poi.getTitle())
            .setMessage(
                "地址：" + (poi.getSnippet().isEmpty() ? poi.getCityName() + poi.getAdName() : poi.getSnippet()) + "\n" +
                "距离：" + (poi.getDistance() > 0 ? poi.getDistance() + "m" : "未知") + "\n" +
                "电话：" + (poi.getTel().isEmpty() ? "暂无" : poi.getTel())
            )
            .setPositiveButton("步行导航", (d, w) -> startWalkRoute(poi))
            .setNeutralButton("打开高德导航", (d, w) -> openAmapNavigation(poi))
            .setNegativeButton("关闭", null)
            .show();
    }

    // ========== 步行路径规划 ==========

    private void startWalkRoute(PoiItem poi) {
        if (routeSearch == null) {
            Toast.makeText(this, "路径规划暂不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLonPoint start = new LatLonPoint(centerPoint.latitude, centerPoint.longitude);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(
            new RouteSearch.FromAndTo(start, poi.getLatLonPoint()));
        routeSearch.calculateWalkRouteAsyn(query);
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        if (errorCode != 1000 || result == null || result.getPaths().isEmpty()) {
            Toast.makeText(this, "路径规划失败", Toast.LENGTH_SHORT).show();
            return;
        }
        WalkPath path = result.getPaths().get(0);
        String info = "步行距离：" + path.getDistance() + "m\n" +
                      "预计时间：" + (path.getDuration() / 60) + "分钟\n" +
                      "步数约：" + path.getSteps().size() + "步";
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("步行路线")
            .setMessage(info)
            .setPositiveButton("好的", null)
            .show();
    }

    @Override
    public void onDriveRouteSearched(com.amap.api.services.route.DriveRouteResult driveRouteResult, int i) {}
    @Override
    public void onRideRouteSearched(com.amap.api.services.route.RideRouteResult rideRouteResult, int i) {}
    @Override
    public void onBusRouteSearched(com.amap.api.services.route.BusRouteResult busRouteResult, int i) {}

    private void openAmapNavigation(PoiItem poi) {
        try {
            String uri = "androidamap://navi?sourceApplication=银发守护者&lat="
                + poi.getLatLonPoint().getLatitude() + "&lon=" + poi.getLatLonPoint().getLongitude()
                + "&poiname=" + poi.getTitle() + "&style=2";
            android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.autonavi.minimap");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // 未安装高德 → 浏览器
                String webUrl = "https://uri.amap.com/navigation?to="
                    + poi.getLatLonPoint().getLongitude() + "," + poi.getLatLonPoint().getLatitude()
                    + ",0&mode=walk&coordinate=gaode";
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(webUrl)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "无法打开导航", Toast.LENGTH_SHORT).show();
        }
    }

    private float getMarkerColor(int index) {
        float[] colors = {
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_VIOLET
        };
        return colors[index % colors.length];
    }

    // ========== 权限回调 ==========

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (aMap != null) aMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "需要定位权限才能显示当前位置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ========== MapView 生命周期 ==========

    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    private int dp(int val) { return Math.round(getResources().getDisplayMetrics().density * val); }
}
