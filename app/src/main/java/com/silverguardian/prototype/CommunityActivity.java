package com.silverguardian.prototype;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class CommunityActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener, RouteSearch.OnRouteSearchListener {

    private static final int REQUEST_LOCATION = 301;

    private final LatLng centerPoint = new LatLng(23.1291, 113.2644);
    private final List<PoiItem> currentPois = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();

    private MapView mapView;
    private AMap aMap;
    private PoiSearch poiSearch;
    private RouteSearch routeSearch;
    private TextView statusText;
    private String selectedKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        bindHeader();
        bindControls();

        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);

        mapView = findViewById(R.id.community_map);
        mapView.onCreate(savedInstanceState);
        initMap();
    }

    private void bindHeader() {
        android.view.View header = findViewById(R.id.community_page_header);
        TextView back = header.findViewById(R.id.header_back);
        TextView title = header.findViewById(R.id.header_title);
        TextView action = header.findViewById(R.id.header_action);
        back.setVisibility(android.view.View.GONE);
        action.setVisibility(android.view.View.GONE);
        title.setText(R.string.community_title);
    }

    private void bindControls() {
        statusText = findViewById(R.id.community_status);
        statusText.setText(R.string.community_activity_status_initializing);

        ((TextView) findViewById(R.id.community_intro)).setText(R.string.community_intro);
        ((TextView) findViewById(R.id.community_back_button)).setText(R.string.community_activity_back_settings);
        findViewById(R.id.community_back_button).setOnClickListener(v -> finish());

        bindChip(findViewById(R.id.community_chip_market), R.string.community_label_market, R.string.community_keyword_market);
        bindChip(findViewById(R.id.community_chip_hospital), R.string.community_label_hospital, R.string.community_keyword_hospital);
        bindChip(findViewById(R.id.community_chip_pharmacy), R.string.community_label_pharmacy, R.string.community_keyword_pharmacy);
        bindChip(findViewById(R.id.community_chip_supermarket), R.string.community_label_supermarket, R.string.community_keyword_supermarket);
        bindChip(findViewById(R.id.community_chip_bank), R.string.community_label_bank, R.string.community_keyword_bank);
    }

    private void bindChip(TextView chip, int labelRes, int keywordRes) {
        chip.setText(labelRes);
        chip.setTag(getString(keywordRes));
        chip.setOnClickListener(v -> searchPoi((String) v.getTag()));
        chips.add(chip);
    }

    private void initMap() {
        aMap = mapView.getMap();
        if (aMap == null) {
            statusText.setText(R.string.community_activity_map_init_failed);
            return;
        }

        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
        locationStyle.interval(2000);
        aMap.setMyLocationStyle(locationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 15));

        try {
            poiSearch = new PoiSearch(this, null);
            poiSearch.setOnPoiSearchListener(this);
        } catch (Exception e) {
            statusText.setText(getString(R.string.community_activity_poi_init_failed, e.getMessage()));
        }

        try {
            routeSearch = new RouteSearch(this);
            routeSearch.setRouteSearchListener(this);
        } catch (Exception ignored) {
            routeSearch = null;
        }

        statusText.setText(R.string.community_activity_ready);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        searchPoi(getString(R.string.community_keyword_market));
    }

    private void searchPoi(String keyword) {
        selectedKeyword = keyword;
        updateChipStyles();

        if (poiSearch == null) {
            Toast.makeText(this, R.string.community_activity_search_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText(getString(R.string.community_status_prefix) + getString(R.string.community_searching_pattern, keyword));

        PoiSearch.Query query = new PoiSearch.Query(keyword, "", getString(R.string.community_activity_city));
        query.setPageSize(20);
        query.setPageNum(0);
        PoiSearch.SearchBound bound = new PoiSearch.SearchBound(
            new LatLonPoint(centerPoint.latitude, centerPoint.longitude), 5000);
        poiSearch.setQuery(query);
        poiSearch.setBound(bound);
        poiSearch.searchPOIAsyn();
    }

    private void updateChipStyles() {
        for (TextView chip : chips) {
            boolean selected = chip.getTag() != null && chip.getTag().equals(selectedKeyword);
            chip.setBackgroundResource(selected ? R.drawable.bg_button_primary : R.drawable.bg_chip_soft);
            chip.setTextColor(getColor(selected ? R.color.surface_white : R.color.primary_dark));
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int errorCode) {
        if (errorCode != 1000 || result == null || result.getPois().isEmpty()) {
            statusText.setText(getString(R.string.community_activity_search_failed, errorCode));
            return;
        }

        currentPois.clear();
        currentPois.addAll(result.getPois());
        aMap.clear();
        statusText.setText(getString(R.string.community_activity_found, currentPois.size()));

        for (int i = 0; i < Math.min(currentPois.size(), 20); i++) {
            PoiItem poi = currentPois.get(i);
            LatLng point = new LatLng(poi.getLatLonPoint().getLatitude(), poi.getLatLonPoint().getLongitude());
            String distance = poi.getDistance() > 0 ? poi.getDistance() + "m" : getString(R.string.community_activity_distance_unknown);
            aMap.addMarker(new MarkerOptions()
                .position(point)
                .title(poi.getTitle())
                .snippet(distance)
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(i))));
        }

        PoiItem first = currentPois.get(0);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(first.getLatLonPoint().getLatitude(), first.getLatLonPoint().getLongitude()), 15));

        aMap.setOnMarkerClickListener(marker -> {
            for (PoiItem poi : currentPois) {
                LatLng point = new LatLng(poi.getLatLonPoint().getLatitude(), poi.getLatLonPoint().getLongitude());
                if (marker.getPosition().equals(point)) {
                    showPoiDetail(poi);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
    }

    private void showPoiDetail(PoiItem poi) {
        String address = poi.getSnippet().isEmpty()
            ? poi.getCityName() + poi.getAdName()
            : poi.getSnippet();
        String distance = poi.getDistance() > 0 ? poi.getDistance() + "m" : getString(R.string.community_activity_distance_unknown);
        String phone = poi.getTel().isEmpty() ? getString(R.string.community_activity_phone_empty) : poi.getTel();

        new AlertDialog.Builder(this)
            .setTitle(poi.getTitle())
            .setMessage(getString(R.string.community_activity_poi_detail, address, distance, phone))
            .setPositiveButton(R.string.community_activity_walk_navigation, (d, w) -> startWalkRoute(poi))
            .setNeutralButton(R.string.community_activity_open_amap, (d, w) -> openAmapNavigation(poi))
            .setNegativeButton(R.string.common_close, null)
            .show();
    }

    private void startWalkRoute(PoiItem poi) {
        if (routeSearch == null) {
            Toast.makeText(this, R.string.community_activity_route_unavailable, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.community_activity_route_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        WalkPath path = result.getPaths().get(0);
        String info = getString(
            R.string.community_activity_walk_route_info,
            path.getDistance(),
            (int) (path.getDuration() / 60),
            path.getSteps().size()
        );
        new AlertDialog.Builder(this)
            .setTitle(R.string.community_activity_walk_route_title)
            .setMessage(info)
            .setPositiveButton(R.string.common_ok, null)
            .show();
    }

    @Override public void onDriveRouteSearched(com.amap.api.services.route.DriveRouteResult driveRouteResult, int i) { }
    @Override public void onRideRouteSearched(com.amap.api.services.route.RideRouteResult rideRouteResult, int i) { }
    @Override public void onBusRouteSearched(com.amap.api.services.route.BusRouteResult busRouteResult, int i) { }

    private void openAmapNavigation(PoiItem poi) {
        try {
            String uri = "androidamap://navi?sourceApplication=" + Uri.encode(getString(R.string.community_activity_source_name))
                + "&lat=" + poi.getLatLonPoint().getLatitude()
                + "&lon=" + poi.getLatLonPoint().getLongitude()
                + "&poiname=" + Uri.encode(poi.getTitle())
                + "&style=2";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.autonavi.minimap");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                String webUrl = "https://uri.amap.com/navigation?to="
                    + poi.getLatLonPoint().getLongitude() + "," + poi.getLatLonPoint().getLatitude()
                    + ",0&mode=walk&coordinate=gaode";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.community_activity_nav_open_failed, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (aMap != null) {
                    aMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, R.string.community_activity_location_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}