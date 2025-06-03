package com.example.np_team_k.network.login;
import android.util.Log;

import com.example.np_team_k.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginManager {

    private static final String TAG = "LoginManager";

    public static void login(String kakaoId, LoginCallback callback) {
        LoginAPI userAPI = RetrofitClient.getClient().create(LoginAPI.class);
        LoginRequest request = new LoginRequest(kakaoId);

        userAPI.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "응답 실패: " + response.code());
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage());
                callback.onFailure();
            }
        });
    }

    // 콜백 인터페이스
    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onFailure();
    }
}

