package com.silverguardian.prototype.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 社区便民查询 — 高德 POI 搜索 + 导航。
 * 原型阶段使用模拟定位 + OkHttp 调高德 Web API 搜索附近 POI，
 * 点击导航通过 Intent 调起高德地图 App。
 * 正式集成高德 3D Map SDK 需在 build.gradle 添加依赖并申请 Key。
 */
public class CommunityFragment extends BaseFragment {
    private static final int REQUEST_LOCATION = 201;
    private LinearLayout resultContainer;
    private TextView statusText;

    // 模拟定位（广州商学院附近）
    private final double mockLat = 23.1291;
    private final double mockLng = 113.2644;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getResources().getColor(R.color.bg_page));
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        // 标题
        TextView title = new TextView(requireContext());
        title.setText("社区便民查询");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.text_primary));
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title);

        // 搜索按钮栏
        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout btnRow = new LinearLayout(requireContext());
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 0, 0, dp(16));
        btnRow.addView(searchChip("菜市场", "菜市场"));
        btnRow.addView(searchChip("社区医院", "社区卫生服务中心"));
        btnRow.addView(searchChip("药店", "药店"));
        btnRow.addView(searchChip("超市", "超市"));
        btnRow.addView(searchChip("银行", "银行"));
        hsv.addView(btnRow);
        root.addView(hsv);

        // 当前位置提示
        statusText = new TextView(requireContext());
        statusText.setText("当前位置附近（演示：广州商学院周边）");
        statusText.setTextSize(14);
        statusText.setTextColor(getResources().getColor(R.color.text_secondary));
        statusText.setPadding(0, 0, 0, dp(16));
        root.addView(statusText);

        // 结果列表
        resultContainer = new LinearLayout(requireContext());
        resultContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(resultContainer, new LinearLayout.LayoutParams(-1, 0, 1));

        // 请求定位权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        // 默认搜索菜市场
        searchPOI("菜市场");
        return root;
    }

    private View searchChip(String label, String keyword) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTextSize(16);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.setTextColor(getResources().getColor(R.color.primary));
        chip.setBackgroundResource(R.drawable.bg_chip_soft);
        chip.setPadding(dp(16), dp(12), dp(16), dp(12));
        chip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.rightMargin = dp(10);
        chip.setLayoutParams(params);
        chip.setOnClickListener(v -> searchPOI(keyword));
        return chip;
    }

    private void searchPOI(String keyword) {
        statusText.setText("正在搜索附近的「" + keyword + "」...");
        resultContainer.removeAllViews();

        // 模拟 POI 数据（实际应使用高德 POI 搜索 API）
        List<String[]> pois = getMockPOIs(keyword);
        for (String[] poi : pois) {
            resultContainer.addView(poiCard(poi[0], poi[1], poi[2], poi[3]));
        }

        statusText.setText("找到 " + pois.size() + " 个「" + keyword + "」附近结果");
    }

    private List<String[]> getMockPOIs(String keyword) {
        List<String[]> list = new ArrayList<>();
        switch (keyword) {
            case "菜市场":
                list.add(new String[]{"九龙综合市场", "农贸市场", "约 450m", "23.1312,113.2689"});
                list.add(new String[]{"凤凰市场", "菜市场", "约 780m", "23.1256,113.2612"});
                list.add(new String[]{"旺村农贸批发市场", "批发市场", "约 1.2km", "23.1223,113.2701"});
                break;
            case "社区卫生服务中心":
                list.add(new String[]{"九龙镇社区卫生服务中心", "医院", "约 620m", "23.1289,113.2701"});
                list.add(new String[]{"中新广州知识城医院", "综合医院", "约 2.1km", "23.1156,113.2789"});
                list.add(new String[]{"九佛社区卫生站", "卫生站", "约 1.5km", "23.1345,113.2567"});
                break;
            case "药店":
                list.add(new String[]{"大参林药店（九龙大道店）", "药店", "约 320m", "23.1301,113.2678"});
                list.add(new String[]{"海王星辰健康药房", "药店", "约 550m", "23.1278,113.2634"});
                list.add(new String[]{"老百姓大药房", "药店", "约 800m", "23.1256,113.2712"});
                list.add(new String[]{"仁和堂药房", "药店", "约 1.0km", "23.1223,113.2598"});
                break;
            case "超市":
                list.add(new String[]{"华润万家（知识城店）", "超市", "约 600m", "23.1289,113.2712"});
                list.add(new String[]{"沃尔玛购物广场", "超市", "约 2.5km", "23.1123,113.2891"});
                break;
            case "银行":
                list.add(new String[]{"中国工商银行（知识城支行）", "银行", "约 500m", "23.1298,113.2698"});
                list.add(new String[]{"中国建设银行", "银行", "约 750m", "23.1267,113.2723"});
                break;
            default:
                list.add(new String[]{"未找到相关结果", "", "请尝试其他关键词", ""});
        }
        return list;
    }

    private View poiCard(String name, String type, String distance, String latlng) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackgroundResource(R.drawable.bg_card_surface);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = 0;
        params.bottomMargin = dp(10);
        card.setLayoutParams(params);

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextSize(18);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        nameView.setTextColor(getResources().getColor(R.color.text_primary));
        texts.addView(nameView);

        TextView metaView = new TextView(requireContext());
        metaView.setText(type + " · " + distance);
        metaView.setTextSize(14);
        metaView.setTextColor(getResources().getColor(R.color.text_secondary));
        metaView.setPadding(0, dp(4), 0, 0);
        texts.addView(metaView);
        card.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        // 导航按钮
        TextView navBtn = new TextView(requireContext());
        navBtn.setText("导航");
        navBtn.setTextSize(14);
        navBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        navBtn.setTextColor(getResources().getColor(R.color.primary));
        navBtn.setBackgroundResource(R.drawable.bg_chip_soft);
        navBtn.setPadding(dp(14), dp(10), dp(14), dp(10));
        navBtn.setOnClickListener(v -> navigateToPOI(name, latlng));
        card.addView(navBtn);
        return card;
    }

    private void navigateToPOI(String name, String latlng) {
        if (latlng.isEmpty()) return;
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("androidamap://navi?sourceApplication=银发守护者&lat=" + latlng.split(",")[0] + "&lon=" + latlng.split(",")[1] + "&poiname=" + name + "&style=2"));
            intent.setPackage("com.autonavi.minimap");
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // 未安装高德地图 → 浏览器打开高德导航
                android.content.Intent webIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://uri.amap.com/navigation?to=" + latlng.replace(",", ",") + ",0&mode=walk&coordinate=gaode"));
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "无法打开导航", Toast.LENGTH_SHORT).show();
        }
    }

}
