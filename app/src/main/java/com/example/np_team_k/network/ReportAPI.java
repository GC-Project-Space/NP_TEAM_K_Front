package com.example.np_team_k.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportAPI {

    // 감정 비율 데이터 조회
    @GET("/report/emotion")
    Call<EmotionResponse> getEmotion(@Query("kakaoId") String kakaoId);

    // 공감 비율 데이터 조회
    @GET("/report/reaction")
    Call<ReactionResponse> getReaction(@Query("kakaoId") String kakaoId);

    // 업로드 수 조회
    @GET("/status/weekly")
    Call<WeeklyActivityResponse> getWeeklyActivity(@Query("kakaoId") String kakaoId);

}

