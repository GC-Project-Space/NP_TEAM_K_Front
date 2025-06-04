package com.example.np_team_k.network;

import com.example.np_team_k.domain.model.StatusResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface StatusAPI {
    @GET("/status")
    Call<List<StatusResponse>> getStatusList(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("sort") String sort,
            @Query("kakaoId") String kakaoId
    );
}