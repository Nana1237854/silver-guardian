package com.silverguardian.prototype.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.silverguardian.prototype.ChatDetailActivity;
import com.silverguardian.prototype.MainActivity;
import com.silverguardian.prototype.R;

public class HomeFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.home_ai_action).setOnClickListener(
            v -> startActivity(new Intent(requireContext(), ChatDetailActivity.class)));
        root.findViewById(R.id.home_health_action).setOnClickListener(v -> main().openHealth());
        root.findViewById(R.id.home_medicine_action).setOnClickListener(v -> main().openMedicine());
        root.findViewById(R.id.home_album_action).setOnClickListener(v -> main().openAlbum());
        root.findViewById(R.id.home_settings_action).setOnClickListener(v -> main().openSettings());
        return root;
    }

    private MainActivity main() {
        return (MainActivity) requireActivity();
    }
}
