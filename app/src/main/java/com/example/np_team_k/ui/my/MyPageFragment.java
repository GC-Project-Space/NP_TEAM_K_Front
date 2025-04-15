package com.example.np_team_k.ui.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.np_team_k.databinding.FragmentMyPageBinding;
import com.example.np_team_k.ui.my.adapter.MyStateHistoryAdapter;
import com.example.np_team_k.ui.my.model.MyStateItem;

import java.util.ArrayList;
import java.util.List;

public class MyPageFragment extends Fragment {

    private FragmentMyPageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyPageViewModel myPageViewModel =
                new ViewModelProvider(this).get(MyPageViewModel.class);


        binding = FragmentMyPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 리사이클러뷰 세팅
        setupRecyclerView();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        // 샘플 데이터
        List<MyStateItem> itemList = new ArrayList<>();
        itemList.add(new MyStateItem("모바일 개발자 해야겠다."));
        itemList.add(new MyStateItem("오늘은 디자인 패턴 공부."));
        itemList.add(new MyStateItem("내일은 프로젝트 회의."));

        // 어댑터 연결
        MyStateHistoryAdapter adapter = new MyStateHistoryAdapter(itemList);
        binding.rcyMyHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rcyMyHistory.setAdapter(adapter);
    }
}