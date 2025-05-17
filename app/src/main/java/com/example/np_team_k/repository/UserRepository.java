package com.example.np_team_k.repository;

import android.content.Context;

import com.example.np_team_k.data.UserPreferences;
import com.example.np_team_k.domain.model.User;

import kotlinx.coroutines.flow.Flow;

// 저장 조회 추상화
public class UserRepository {

    private final Context context;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext(); // context 보관
    }

    // 저장
    public void saveUser(User user) {
        UserPreferences.INSTANCE.saveKakaoId(context, user.getKakaoId());
    }

    // 조회 (Flow<Long> 반환)
    public Flow<String> getKakaoIdFlow() {
        return UserPreferences.INSTANCE.getKakaoId(context);
    }
}
