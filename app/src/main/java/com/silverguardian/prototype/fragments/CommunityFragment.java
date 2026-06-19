package com.silverguardian.prototype.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silverguardian.prototype.R;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends BaseFragment {
    private static final int REQUEST_LOCATION = 201;

    private final List<PoiItem> visible = new ArrayList<>();
    private final List<TextView> chips = new ArrayList<>();
    private CommunityAdapter adapter;
    private TextView statusText;
    private TextView emptyState;
    private String selectedKeyword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        bindHeader(view);
        bindControls(view);
        bindList(view);
        ensureLocationPermission();
        searchPoi(getString(R.string.community_keyword_market));
        return view;
    }

    private void bindHeader(View root) {
        View header = root.findViewById(R.id.community_header);
        header.findViewById(R.id.header_back).setVisibility(View.GONE);
        ((TextView) header.findViewById(R.id.header_title)).setText(R.string.community_title);
        header.findViewById(R.id.header_action).setVisibility(View.GONE);
    }

    private void bindControls(View root) {
        statusText = root.findViewById(R.id.community_status);
        statusText.setBackgroundResource(R.drawable.bg_card_surface);
        statusText.setTextColor(color(R.color.text_secondary));

        ((TextView) root.findViewById(R.id.community_intro)).setText(R.string.community_intro);

        emptyState = root.findViewById(R.id.community_empty_state);
        emptyState.setText(R.string.community_empty);

        bindChip(root.findViewById(R.id.community_chip_market), R.string.community_label_market, R.string.community_keyword_market);
        bindChip(root.findViewById(R.id.community_chip_hospital), R.string.community_label_hospital, R.string.community_keyword_hospital);
        bindChip(root.findViewById(R.id.community_chip_pharmacy), R.string.community_label_pharmacy, R.string.community_keyword_pharmacy);
        bindChip(root.findViewById(R.id.community_chip_supermarket), R.string.community_label_supermarket, R.string.community_keyword_supermarket);
        bindChip(root.findViewById(R.id.community_chip_bank), R.string.community_label_bank, R.string.community_keyword_bank);
    }

    private void bindChip(TextView chip, int labelRes, int keywordRes) {
        chip.setText(labelRes);
        chip.setTag(getString(keywordRes));
        chip.setOnClickListener(v -> searchPoi((String) v.getTag()));
        chips.add(chip);
    }

    private void bindList(View root) {
        RecyclerView list = root.findViewById(R.id.community_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommunityAdapter();
        list.setAdapter(adapter);
    }

    private void ensureLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void searchPoi(String keyword) {
        selectedKeyword = keyword;
        updateChipStyles();
        statusText.setText(getString(R.string.community_status_message, getString(R.string.community_searching_pattern, keyword)));
        visible.clear();
        visible.addAll(getMockPois(keyword));
        statusText.setText(getString(R.string.community_status_message, getString(R.string.community_found_pattern, visible.size(), keyword)));
        emptyState.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void updateChipStyles() {
        for (TextView chip : chips) {
            boolean selected = chip.getTag() != null && chip.getTag().equals(selectedKeyword);
            chip.setBackgroundResource(selected ? R.drawable.bg_button_primary : R.drawable.bg_chip_soft);
            chip.setTextColor(color(selected ? R.color.surface_white : R.color.primary_dark));
        }
    }

    private List<PoiItem> getMockPois(String keyword) {
        List<PoiItem> items = new ArrayList<>();
        if (matchesKeyword(keyword, R.string.community_keyword_market)) {
            items.add(poi(R.string.community_poi_market_jiulong_name, R.string.community_poi_market_jiulong_type, R.string.community_poi_market_jiulong_distance, R.string.community_poi_market_jiulong_latlng));
            items.add(poi(R.string.community_poi_market_fenghuang_name, R.string.community_poi_market_fenghuang_type, R.string.community_poi_market_fenghuang_distance, R.string.community_poi_market_fenghuang_latlng));
            items.add(poi(R.string.community_poi_market_xucun_name, R.string.community_poi_market_xucun_type, R.string.community_poi_market_xucun_distance, R.string.community_poi_market_xucun_latlng));
        } else if (matchesKeyword(keyword, R.string.community_keyword_hospital)) {
            items.add(poi(R.string.community_poi_hospital_jiulong_name, R.string.community_poi_hospital_jiulong_type, R.string.community_poi_hospital_jiulong_distance, R.string.community_poi_hospital_jiulong_latlng));
            items.add(poi(R.string.community_poi_hospital_knowledge_city_name, R.string.community_poi_hospital_knowledge_city_type, R.string.community_poi_hospital_knowledge_city_distance, R.string.community_poi_hospital_knowledge_city_latlng));
            items.add(poi(R.string.community_poi_hospital_jiufu_name, R.string.community_poi_hospital_jiufu_type, R.string.community_poi_hospital_jiufu_distance, R.string.community_poi_hospital_jiufu_latlng));
        } else if (matchesKeyword(keyword, R.string.community_keyword_pharmacy)) {
            items.add(poi(R.string.community_poi_pharmacy_dashenlin_name, R.string.community_poi_pharmacy_type, R.string.community_poi_pharmacy_dashenlin_distance, R.string.community_poi_pharmacy_dashenlin_latlng));
            items.add(poi(R.string.community_poi_pharmacy_neptune_name, R.string.community_poi_pharmacy_type, R.string.community_poi_pharmacy_neptune_distance, R.string.community_poi_pharmacy_neptune_latlng));
            items.add(poi(R.string.community_poi_pharmacy_laobaixing_name, R.string.community_poi_pharmacy_type, R.string.community_poi_pharmacy_laobaixing_distance, R.string.community_poi_pharmacy_laobaixing_latlng));
            items.add(poi(R.string.community_poi_pharmacy_renhetang_name, R.string.community_poi_pharmacy_type, R.string.community_poi_pharmacy_renhetang_distance, R.string.community_poi_pharmacy_renhetang_latlng));
        } else if (matchesKeyword(keyword, R.string.community_keyword_supermarket)) {
            items.add(poi(R.string.community_poi_supermarket_crv_name, R.string.community_poi_supermarket_crv_type, R.string.community_poi_supermarket_crv_distance, R.string.community_poi_supermarket_crv_latlng));
            items.add(poi(R.string.community_poi_supermarket_walmart_name, R.string.community_poi_supermarket_walmart_type, R.string.community_poi_supermarket_walmart_distance, R.string.community_poi_supermarket_walmart_latlng));
        } else if (matchesKeyword(keyword, R.string.community_keyword_bank)) {
            items.add(poi(R.string.community_poi_bank_icbc_name, R.string.community_poi_bank_type, R.string.community_poi_bank_icbc_distance, R.string.community_poi_bank_icbc_latlng));
            items.add(poi(R.string.community_poi_bank_ccb_name, R.string.community_poi_bank_type, R.string.community_poi_bank_ccb_distance, R.string.community_poi_bank_ccb_latlng));
        }
        return items;
    }

    private PoiItem poi(int nameRes, int typeRes, int distanceRes, int latlngRes) {
        return new PoiItem(getString(nameRes), getString(typeRes), getString(distanceRes), getString(latlngRes));
    }

    private boolean matchesKeyword(String keyword, int keywordRes) {
        return getString(keywordRes).equals(keyword);
    }

    private void confirmNavigate(PoiItem item) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.community_nav)
            .setMessage(getString(R.string.community_nav_dialog, item.name))
            .setPositiveButton(R.string.community_nav_confirm, (dialog, which) -> navigateToPoi(item))
            .setNegativeButton(R.string.common_cancel, null)
            .show();
    }

    private void navigateToPoi(PoiItem item) {
        if (item.latlng == null || item.latlng.isEmpty()) {
            Toast.makeText(requireContext(), R.string.community_nav_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String[] parts = item.latlng.split(",");
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("androidamap://navi?sourceApplication=" + Uri.encode(getString(R.string.community_activity_source_name))
                    + "&lat=" + parts[0]
                    + "&lon=" + parts[1]
                    + "&poiname=" + Uri.encode(item.name)
                    + "&style=2"));
            intent.setPackage("com.autonavi.minimap");
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://uri.amap.com/navigation?to=" + item.latlng + ",0&mode=walk&coordinate=gaode"));
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.community_nav_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
            && grantResults.length > 0
            && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            toast(getString(R.string.community_location_permission));
        }
    }

    private class CommunityAdapter extends RecyclerView.Adapter<CommunityHolder> {
        @NonNull
        @Override
        public CommunityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_poi, parent, false);
            return new CommunityHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommunityHolder holder, int position) {
            holder.bind(visible.get(position));
        }

        @Override
        public int getItemCount() {
            return visible.size();
        }
    }

    private class CommunityHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView type;
        private final TextView distance;
        private final TextView nav;

        CommunityHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.community_poi_name);
            type = itemView.findViewById(R.id.community_poi_type);
            distance = itemView.findViewById(R.id.community_poi_distance);
            nav = itemView.findViewById(R.id.community_poi_nav);
        }

        void bind(PoiItem item) {
            name.setText(item.name);
            type.setText(item.type);
            distance.setText(getString(R.string.community_distance_meta, item.distance, getString(R.string.community_walk_reachable)));
            itemView.setOnClickListener(v -> confirmNavigate(item));
            nav.setOnClickListener(v -> navigateToPoi(item));
        }
    }

    private static class PoiItem {
        final String name;
        final String type;
        final String distance;
        final String latlng;

        PoiItem(String name, String type, String distance, String latlng) {
            this.name = name;
            this.type = type;
            this.distance = distance;
            this.latlng = latlng;
        }
    }
}