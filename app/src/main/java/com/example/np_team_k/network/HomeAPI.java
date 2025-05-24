package com.example.np_team_k.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;

public interface HomeAPI {

    // 상태 목록 조회 (GET)
    @GET("status")
    Call<PinResponse> getPins(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("sort") String sort,
            @Query("kakaoId") String kakaoId
    );

    // 상태 등록 (POST)
    @POST("/status")
    Call<Void> postStatus(@Body PinRequest request);


    // 상태 삭제 (DELETE)
    @DELETE("status/{id}")
    Call<Void> deleteStatus(
            @Path("id") String id,
            @Query("kakaoId") String kakaoId
    );
}
