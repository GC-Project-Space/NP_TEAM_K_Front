package com.example.np_team_k.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;

import com.example.np_team_k.R;
import com.example.np_team_k.databinding.FragmentListPageBinding;

import java.util.ArrayList;

public class ListPageFragment extends Fragment {

    private FragmentListPageBinding binding;
    private ListPageViewModel viewModel;
    private ListAdapter adapter;

    private double currentLat = 37.123;   // 예시 좌표, 네 위치 정보로 바꿔줘
    private double currentLng = 127.456;
    private String currentKakaoId = "sample_kakao_id"; // 실제 Kakao ID 값으로 교체 필요

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListPageBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ListPageViewModel.class);

        adapter = new ListAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        observeViewModel();
        setupToggleButtons();

        // 기본 정렬: 인기순
        updateToggleUI(true);
        viewModel.fetchStatusList(currentLat, currentLng, "popular", currentKakaoId);

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getSortedList().observe(getViewLifecycleOwner(), listItems -> {
            adapter.setItems(listItems);
        });
    }


    private void setupToggleButtons() {

        binding.btnPopular.setOnClickListener(v -> {
            updateToggleUI(true);  // 인기순 선택
            viewModel.fetchStatusList(currentLat, currentLng, "popular", currentKakaoId);
        });

        binding.btnDistance.setOnClickListener(v -> {
            updateToggleUI(false); // 거리순 선택
            viewModel.fetchStatusList(currentLat, currentLng, "distance", currentKakaoId);
        });
    }

    private void updateToggleUI(boolean isPopularSelected) {
        if (isPopularSelected) {
            // 인기순: 선택됨
            binding.btnPopular.setBackgroundResource(R.drawable.bg_toggle_selected);
            binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));

            // 거리순: 비선택
            binding.btnDistance.setBackgroundResource(R.drawable.bg_toggle_unselected);
            binding.btnDistance.setTextColor(ContextCompat.getColor(requireContext(), R.color.black80));
        } else {
            // 거리순: 선택됨
            binding.btnDistance.setBackgroundResource(R.drawable.bg_toggle_selected);
            binding.btnDistance.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));

            // 인기순: 비선택
            binding.btnPopular.setBackgroundResource(R.drawable.bg_toggle_unselected);
            binding.btnPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.black80));
        }
    }
}
