package com.example.np_team_k.network;

import com.example.np_team_k.domain.model.StatusResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StatusManager {
    private StatusAPI statusAPI;

    public StatusManager() {
        Retrofit retrofit = RetrofitClient.getClient();
        statusAPI = retrofit.create(StatusAPI.class);
    }

    public Call<List<StatusResponse>> getStatuses(double lat, double lng, String sort, String kakaoId) {
        return statusAPI.getStatusList(lat, lng, sort, kakaoId);
    }
}
