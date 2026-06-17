package com.silverguardian.prototype.fragments;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.silverguardian.prototype.utils.FontScaleHelper;

/**
 * 所有 Fragment 的基类 — 消除 dp/sp/color/toast 重复。
 */
public abstract class BaseFragment extends Fragment {

    protected int dp(int value) {
        return FontScaleHelper.dp(requireContext(), value);
    }

    protected int sp(int base) {
        return FontScaleHelper.sp(requireContext(), base);
    }

    protected int color(int resId) {
        return getResources().getColor(resId);
    }

    protected void toast(@NonNull String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
