package com.silverguardian.prototype;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends BaseActivity {
    private static final int REQUEST_LOCATION = 301;

    private final List<PoiItem> currentPois = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private final LatLng defaultCenter = new LatLng(23.1291, 113.2644);

    private MapView mapView;
    private AMap aMap;
    private TextView statusText;
    private String selectedKeyword;
    private AMapLocationClient locationClient;
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private AMapLocation lastLocation;
    private boolean locationReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        bindHeader();
        bindControls();

        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);

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
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        locationStyle.interval(2000);
        aMap.setMyLocationStyle(locationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setLocationSource(locationSource);
        aMap.setMyLocationEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 15));
        statusText.setText(R.string.community_activity_ready);

        if (!communitySearch().hasLocationPermission(this)) {
            ActivityCompat.requestPermissions(this, communitySearch().getLocationPermissions(), REQUEST_LOCATION);
        } else {
            startLocation();
        }
    }

    private final LocationSource locationSource = new LocationSource() {
        @Override
        public void activate(OnLocationChangedListener listener) {
            locationChangedListener = listener;
        }

        @Override
        public void deactivate() {
            locationChangedListener = null;
        }
    };

    private void startLocation() {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setInterval(2000);
            option.setOnceLocation(false);
            locationClient.setLocationOption(option);
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation loc) {
                    if (loc == null || loc.getErrorCode() != 0) return;

                    lastLocation = loc;
                    communitySearch().setSearchCenter(
                        new LatLonPoint(loc.getLatitude(), loc.getLongitude()));

                    if (locationChangedListener != null) {
                        locationChangedListener.onLocationChanged(loc);
                    }

                    if (!locationReady) {
                        locationReady = true;
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(loc.getLatitude(), loc.getLongitude()), 15));
                        searchPoi(communitySearch().defaultKeyword());
                    }
                }
            });
            locationClient.startLocation();
        } catch (Exception e) {
            locationClient = null;
            searchPoi(communitySearch().defaultKeyword());
        }
    }

    private void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
        locationChangedListener = null;
        locationReady = false;
    }

    private void searchPoi(String keyword) {
        selectedKeyword = keyword;
        updateChipStyles();
        communitySearch().search(keyword, new com.silverguardian.prototype.community.CommunityPoiSearchModule.SearchCallback() {
            @Override
            public void onLoading(String status) {
                statusText.setText(status);
            }

            @Override
            public void onSuccess(String status, List<PoiItem> pois) {
                currentPois.clear();
                currentPois.addAll(pois);
                statusText.setText(status);
                renderPois();
            }

            @Override
            public void onError(String status) {
                currentPois.clear();
                statusText.setText(status);
                if (aMap != null) aMap.clear();
            }
        });
    }

    private void renderPois() {
        if (aMap == null) return;
        aMap.clear();
        for (int i = 0; i < Math.min(currentPois.size(), 20); i++) {
            PoiItem poi = currentPois.get(i);
            LatLng point = new LatLng(poi.getLatLonPoint().getLatitude(), poi.getLatLonPoint().getLongitude());
            aMap.addMarker(new MarkerOptions()
                .position(point)
                .title(poi.getTitle())
                .snippet(communitySearch().markerDistance(poi))
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(i))));
        }
        PoiItem first = currentPois.isEmpty() ? null : currentPois.get(0);
        if (first != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(first.getLatLonPoint().getLatitude(), first.getLatLonPoint().getLongitude()), 15));
        }
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

    private void updateChipStyles() {
        for (TextView chip : chips) {
            boolean selected = chip.getTag() != null && chip.getTag().equals(selectedKeyword);
            chip.setBackgroundResource(selected ? R.drawable.bg_button_primary : R.drawable.bg_chip_soft);
            chip.setTextColor(getColor(selected ? R.color.surface_white : R.color.primary_dark));
        }
    }

    private void showPoiDetail(PoiItem poi) {
        new AlertDialog.Builder(this)
            .setTitle(poi.getTitle())
            .setMessage(communitySearch().buildPoiDetail(poi))
            .setPositiveButton(R.string.community_activity_walk_navigation, (d, w) -> communitySearch().requestWalkRoute(poi, new com.silverguardian.prototype.community.CommunityPoiSearchModule.RouteCallback() {
                @Override public void onSuccess(String routeInfo) { showRoute(routeInfo); }
                @Override public void onError(String message) { Toast.makeText(CommunityActivity.this, message, Toast.LENGTH_SHORT).show(); }
            }))
            .setNeutralButton(R.string.community_activity_open_amap, (d, w) -> {
                if (!communitySearch().openNavigation(this, poi)) {
                    Toast.makeText(this, R.string.community_activity_nav_open_failed, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.common_close, null)
            .show();
    }

    private void showRoute(String routeInfo) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.community_activity_walk_route_title)
            .setMessage(routeInfo)
            .setPositiveButton(R.string.common_ok, null)
            .show();
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
                startLocation();
            } else {
                Toast.makeText(this, R.string.community_activity_location_permission, Toast.LENGTH_SHORT).show();
                searchPoi(communitySearch().defaultKeyword());
            }
        }
    }

    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); stopLocation(); if (mapView != null) mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); stopLocation(); if (mapView != null) mapView.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
