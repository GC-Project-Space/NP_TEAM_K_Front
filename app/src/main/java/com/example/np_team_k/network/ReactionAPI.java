package com.example.np_team_k.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReactionAPI {

    // 리액션 추가
    @POST("/status/{id}/react/{type}")
    Call<ResponseBody> addReaction(
            @Path("id") String pinId,
            @Path("type") String reactionType,
            @Query("kakaoId") String kakaoId
    );

    // 리액션 취소
    @DELETE("/status/{id}/react/{type}")
    Call<ResponseBody> removeReaction(
            @Path("id") String pinId,
            @Path("type") String reactionType,
            @Query("kakaoId") String kakaoId
    );
}
