package com.example.np_team_k.network.login;

public class LoginRequest {
    private String kakaoId;

    public LoginRequest(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }
}
