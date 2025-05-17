package com.example.np_team_k.domain.model;

public class User {
    private final String kakaoId;

    public User(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getKakaoId() {
        return kakaoId;
    }
}
