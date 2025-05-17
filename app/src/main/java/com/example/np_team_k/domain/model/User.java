package com.example.np_team_k.domain.model;

public class User {
    private final String kakaoId;
    private final boolean isMember;

    public User(String kakaoId, boolean isMember) {
        this.kakaoId = kakaoId;
        this.isMember = isMember;
    }

    public String getKakaoId() {
        return kakaoId;
    }


    public boolean getIsMember() {
        return isMember;
    }
}
