package com.silverguardian.prototype.community;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.silverguardian.prototype.R;

import java.util.List;

public class CommunityPoiSearchModule {
    public interface SearchCallback {
        void onLoading(String status);
        void onSuccess(String status, List<PoiItem> pois);
        void onError(String status);
    }

    public interface RouteCallback {
        void onSuccess(String routeInfo);
        void onError(String message);
    }

    private final Context appContext;
    private final LatLonPoint fallbackCenter = new LatLonPoint(23.1291, 113.2644);
    private LatLonPoint searchCenter;

    public CommunityPoiSearchModule(Context context) {
        appContext = context.getApplicationContext();
        ServiceSettings.updatePrivacyShow(appContext, true, true);
        ServiceSettings.updatePrivacyAgree(appContext, true);
    }

    public void setSearchCenter(LatLonPoint point) {
        this.searchCenter = point;
    }

    public String[] getLocationPermissions() {
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    }

    public boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public String defaultKeyword() {
        return appContext.getString(R.string.community_keyword_market);
    }

    public void search(String keyword, SearchCallback callback) {
        callback.onLoading(appContext.getString(R.string.community_status_prefix) + appContext.getString(R.string.community_searching_pattern, keyword));
        try {
            PoiSearch.Query query = new PoiSearch.Query(keyword, "", appContext.getString(R.string.community_activity_city));
            query.setPageSize(20);
            query.setPageNum(0);
            PoiSearch.SearchBound bound = new PoiSearch.SearchBound(
                searchCenter != null ? searchCenter : fallbackCenter, 5000);
            PoiSearch poiSearch = new PoiSearch(appContext, query);
            poiSearch.setBound(bound);
            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult result, int errorCode) {
                    if (errorCode != 1000 || result == null || result.getPois() == null || result.getPois().isEmpty()) {
                        callback.onError(appContext.getString(R.string.community_activity_search_failed, errorCode));
                        return;
                    }
                    callback.onSuccess(appContext.getString(R.string.community_activity_found, result.getPois().size()), result.getPois());
                }

                @Override
                public void onPoiItemSearched(PoiItem poiItem, int i) {
                }
            });
            poiSearch.searchPOIAsyn();
        } catch (Exception e) {
            callback.onError(appContext.getString(R.string.community_activity_poi_init_failed, e.getMessage()));
        }
    }

    public String buildPoiDetail(PoiItem poi) {
        String address = poi.getSnippet() == null || poi.getSnippet().isEmpty()
            ? poi.getCityName() + poi.getAdName()
            : poi.getSnippet();
        String distance = poi.getDistance() > 0 ? poi.getDistance() + "m" : appContext.getString(R.string.community_activity_distance_unknown);
        String phone = poi.getTel() == null || poi.getTel().isEmpty() ? appContext.getString(R.string.community_activity_phone_empty) : poi.getTel();
        return appContext.getString(R.string.community_activity_poi_detail, address, distance, phone);
    }

    public String markerDistance(PoiItem poi) {
        return poi.getDistance() > 0 ? poi.getDistance() + "m" : appContext.getString(R.string.community_activity_distance_unknown);
    }

    public void requestWalkRoute(PoiItem poi, RouteCallback callback) {
        try {
            RouteSearch routeSearch = new RouteSearch(appContext);
            routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
                @Override public void onBusRouteSearched(com.amap.api.services.route.BusRouteResult busRouteResult, int i) { }
                @Override public void onDriveRouteSearched(com.amap.api.services.route.DriveRouteResult driveRouteResult, int i) { }
                @Override public void onRideRouteSearched(com.amap.api.services.route.RideRouteResult rideRouteResult, int i) { }

                @Override
                public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
                    if (errorCode != 1000 || result == null || result.getPaths() == null || result.getPaths().isEmpty()) {
                        callback.onError(appContext.getString(R.string.community_activity_route_failed));
                        return;
                    }
                    WalkPath path = result.getPaths().get(0);
                    callback.onSuccess(appContext.getString(
                        R.string.community_activity_walk_route_info,
                        path.getDistance(),
                        (int) (path.getDuration() / 60),
                        path.getSteps().size()
                    ));
                }
            });
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(
                new RouteSearch.FromAndTo(
                    searchCenter != null ? searchCenter : fallbackCenter,
                    poi.getLatLonPoint())
            );
            routeSearch.calculateWalkRouteAsyn(query);
        } catch (Exception e) {
            callback.onError(appContext.getString(R.string.community_activity_route_unavailable));
        }
    }

    public boolean openNavigation(Context context, PoiItem poi) {
        try {
            String uri = "androidamap://navi?sourceApplication=" + Uri.encode(appContext.getString(R.string.community_activity_source_name))
                + "&lat=" + poi.getLatLonPoint().getLatitude()
                + "&lon=" + poi.getLatLonPoint().getLongitude()
                + "&poiname=" + Uri.encode(poi.getTitle())
                + "&style=2";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.autonavi.minimap");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                String webUrl = "https://uri.amap.com/navigation?to="
                    + poi.getLatLonPoint().getLongitude() + "," + poi.getLatLonPoint().getLatitude()
                    + ",0&mode=walk&coordinate=gaode";
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
