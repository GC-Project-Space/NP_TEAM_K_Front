package com.example.np_team_k.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

import com.example.np_team_k.network.StatusMineResponse;

public interface MyPageAPI {

    // 내가 작성한 상태 목록 조회 API
    @GET("/status/mine")
    Call<List<StatusMineResponse>> getStatusMine(
            @Query("kakaold") String kakaoId,
            @Query("sort") String sort // 정렬 방식: recent, popular (선택, 기본: recent)
    );
}

