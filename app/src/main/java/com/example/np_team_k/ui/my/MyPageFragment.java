package com.example.np_team_k.ui.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.np_team_k.databinding.FragmentMyPageBinding;
import com.example.np_team_k.network.StatusMineResponse;
import com.example.np_team_k.ui.my.adapter.MyStateHistoryAdapter;

import java.util.List;

public class MyPageFragment extends Fragment {

    private FragmentMyPageBinding binding;
    private MyPageViewModel myPageViewModel;
    private MyStateHistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myPageViewModel = new ViewModelProvider(this).get(MyPageViewModel.class);
        binding = FragmentMyPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 상태 레포트 페이지 이동
        binding.btnReportPage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StatusReportActivity.class);
            startActivity(intent);
        });

        // 리사이클러뷰 세팅
        setupRecyclerView();
        observeData();

        binding.btnSortPopular.setOnClickListener(v -> {
            myPageViewModel.fetchStatusList("1234", "popular");

            // 상태 설정
            binding.btnSortPopular.setSelected(true);
            binding.btnSortLatest.setSelected(false);
        });

        binding.btnSortLatest.setOnClickListener(v -> {
            myPageViewModel.fetchStatusList("1234", "recent");

            binding.btnSortPopular.setSelected(false);
            binding.btnSortLatest.setSelected(true);

        });

        // 임시로 kakaoId, sort 값 넣기 (나중에 로그인 정보로 교체!)
        String kakaoId = "1234";
        String sort = "recent";

        myPageViewModel.fetchStatusList(kakaoId, sort);

        return root;
    }

    private void setupRecyclerView() {
        adapter = new MyStateHistoryAdapter();
        binding.rcyMyHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcyMyHistory.setAdapter(adapter);
    }

    private void observeData() {
        myPageViewModel.getStatusList().observe(getViewLifecycleOwner(), new Observer<List<StatusMineResponse>>() {
            @Override
            public void onChanged(List<StatusMineResponse> items) {
                adapter.setData(items); // 어댑터에 데이터 전달
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
