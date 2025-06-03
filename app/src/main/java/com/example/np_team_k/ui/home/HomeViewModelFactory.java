package com.example.np_team_k.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.np_team_k.repository.UserRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository userRepository;

    public HomeViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(userRepository);  // 생성자에 UserRepository 주입
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}