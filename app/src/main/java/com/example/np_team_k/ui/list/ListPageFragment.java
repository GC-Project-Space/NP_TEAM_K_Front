package com.example.np_team_k.ui.list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.np_team_k.databinding.FragmentListPageBinding;

public class ListPageFragment extends Fragment {

    private FragmentListPageBinding binding;
    private ListPageViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListPageBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ListPageViewModel.class);

        ListItemAdapter adapter = new ListItemAdapter(new ReactionClickHandler(viewModel));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        viewModel.fetchStatusList(37, 127, "distance", "1234");

        binding.btnSortPopular.setOnClickListener(v -> {
            viewModel.fetchStatusList(37, 127, "popular", "1234");

            // 상태 설정
            binding.btnSortPopular.setSelected(true);
            binding.btnSortLatest.setSelected(false);
        });

        binding.btnSortLatest.setOnClickListener(v -> {
            viewModel.fetchStatusList(37, 127, "distance", "1234");

            binding.btnSortPopular.setSelected(false);
            binding.btnSortLatest.setSelected(true);

        });


        // ListAdapter의 핵심 메서드
        viewModel.getItemList().observe(getViewLifecycleOwner(), list -> {
            Log.d("Fragment", "observe 반응: " + list);
            adapter.submitList(list);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
