package com.example.np_team_k.domain.model;

public class User {
    private final String kakaoId;
    private final String nickname;
    private final boolean isMember;

    public User(String kakaoId, String nickname, boolean isMember) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.isMember = isMember;
    }

    public String getKakaoId() {
        return kakaoId;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean getIsMember() {
        return isMember;
    }
}
