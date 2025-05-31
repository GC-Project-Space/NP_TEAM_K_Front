package com.example.np_team_k.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.example.np_team_k.network.EmotionResponse;
import com.example.np_team_k.network.ReactionResponse;
import com.example.np_team_k.network.ActivityResponse;

public interface ReportAPI {

    // 감정 비율 데이터 조회
    @GET("/report/emotion")
    Call<EmotionResponse> getEmotion(@Query("kakaold") String kakaoId);

    // 공감 비율 데이터 조회
    @GET("/report/reaction")
    Call<ReactionResponse> getReaction(@Query("kakaold") String kakaoId);

    // 업로드 수 조회
    @GET("/report/activity")
    Call<ActivityResponse> getActivity(@Query("kakaold") String kakaoId);
}

