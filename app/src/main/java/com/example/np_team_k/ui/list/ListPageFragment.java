package com.example.np_team_k.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.np_team_k.databinding.FragmentListPageBinding;

public class ListPageFragment extends Fragment {

    private FragmentListPageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListPageViewModel listPageViewModel =
                new ViewModelProvider(this).get(ListPageViewModel.class);

        binding = FragmentListPageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textListPage;
        listPageViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}