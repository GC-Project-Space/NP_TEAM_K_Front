package com.example.np_team_k.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;

import com.example.np_team_k.data.UserPreferences;
import com.example.np_team_k.domain.model.User;

import org.reactivestreams.Publisher;

import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.reactive.ReactiveFlowKt;

import java.util.UUID;

// 저장 조회 추상화
public class UserRepository {

    private final Context context;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public void saveUser(User user) {
        UserPreferences.INSTANCE.saveUser(
                context,
                user.getKakaoId(),
                user.getNickname(),
                user.getIsMember()
        );
    }

    // 카카오 아이디 가져오기
    public Flow<String> getKakaoIdFlow() {
        return UserPreferences.INSTANCE.getKakaoId(context);
    }

    // 지금 유저의 닉네임 가져오기
    public Flow<String> getNicknameFlow() {
        return UserPreferences.INSTANCE.getNickname(context);
    }

    public Flow<Boolean> getIsMemberFlow() {
        return UserPreferences.INSTANCE.getIsMember(context);
    }

    // [추가] Flow → LiveData 변환
    public LiveData<String> getKakaoIdLiveData() {
        Flow<String> flow = getKakaoIdFlow();
        Publisher<String> publisher = ReactiveFlowKt.asPublisher(flow);
        return LiveDataReactiveStreams.fromPublisher(publisher);
    }

    public void saveKakaoId(String kakaoId) {
        String safeId = (kakaoId != null && !kakaoId.isEmpty())
                ? kakaoId
                : "guest_" + UUID.randomUUID().toString();

        // 기존 saveUser() 재활용
        UserPreferences.INSTANCE.saveUser(
                context,
                safeId,
                "게스트",  // 닉네임 기본값 설정
                false     // isMember = false
        );
    }


}