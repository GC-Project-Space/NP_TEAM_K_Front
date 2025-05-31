package com.example.np_team_k.network;

import java.util.Map;

/**
 * 공감 비율 API 응답 데이터 모델
 * 예: { "total": 100, "receivedReactions": { "공감해요": 50, "슬퍼요": 20, ... } }
 */
public class ReactionResponse {
    private int total;
    private Map<String, Integer> receivedReactions;

    public int getTotal() {
        return total;
    }

    public Map<String, Integer> getReceivedReactions() {
        return receivedReactions;
    }
}
