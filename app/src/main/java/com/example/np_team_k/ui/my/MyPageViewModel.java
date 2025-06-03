package com.example.np_team_k.ui.my;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.np_team_k.network.MyPageAPI;
import com.example.np_team_k.network.RetrofitClient;
import com.example.np_team_k.network.StatusMineResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageViewModel extends ViewModel {

    private final MutableLiveData<List<StatusMineResponse>> statusList = new MutableLiveData<>();

    public LiveData<List<StatusMineResponse>> getStatusList() {
        return statusList;
    }

    // 상태 목록 조회 API 호출 메서드
    public void fetchStatusList(String kakaoId, String sort) {
        MyPageAPI api = RetrofitClient.getClient().create(MyPageAPI.class);
        api.getStatusMine(kakaoId, sort).enqueue(new Callback<List<StatusMineResponse>>() {
            @Override
            public void onResponse(Call<List<StatusMineResponse>> call, Response<List<StatusMineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MyPageAPI", "API 호출 성공! 데이터 개수: " + response.body().size());
                } else {
                    Log.e("MyPageAPI", "API 응답 실패! 코드: " + response.code());
                    Log.e("MyPageAPI", "에러 메시지: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<StatusMineResponse>> call, Throwable t) {
                // 실패 시: 필요하면 로그 출력
            }
        });
    }
}