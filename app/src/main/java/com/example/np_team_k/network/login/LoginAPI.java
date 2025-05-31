package com.example.np_team_k.network.login;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginAPI {
    @POST("/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
