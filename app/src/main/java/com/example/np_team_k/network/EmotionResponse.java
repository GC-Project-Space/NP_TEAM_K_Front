package com.example.np_team_k.network;

import java.util.Map;

/**
 * 감정 비율 API 응답 데이터 모델
 * 예: { "total": 30, "emotionCounts": { "슬픔": 40, "불안": 20, ... } }
 */
public class EmotionResponse {
    private int total;
    private Map<String, Integer> emotionCounts;

    public int getTotal() {
        return total;
    }

    public Map<String, Integer> getEmotionCounts() {
        return emotionCounts;
    }
}

