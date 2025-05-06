package com.example.np_team_k.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.np_team_k.databinding.FragmentListPageBinding;

import java.util.ArrayList;
import java.util.List;

public class ListPageFragment extends Fragment {

    private FragmentListPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListPageBinding.inflate(inflater, container, false);

        // 더미 데이터 리스트 생성
        List<ListItem> dummyList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            dummyList.add(new ListItem("모바일 개발자 해야겠다."));
        }

        // 어댑터 연결
        ListAdapter adapter = new ListAdapter(dummyList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
