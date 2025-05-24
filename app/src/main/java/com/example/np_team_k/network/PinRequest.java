package com.example.np_team_k.network;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PinRequest {
    private String writerKakaoId;
    private String message;
    private Location location;
    private ReactionCounts reactionCounts;
    private String createdAt;
    private String myReaction;

    public PinRequest(String writerKakaoId, String message, double latitude, double longitude) {
        this.writerKakaoId = writerKakaoId;
        this.message = message;
        this.location = new Location(latitude, longitude);
        this.createdAt = getCurrentTime();  // ✅ 현재 시각 자동 설정
        this.reactionCounts = new ReactionCounts(); // ✅ 기본값 0으로 초기화
        this.myReaction = null;
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static class Location {
        private double latitude;
        private double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    public static class ReactionCounts {
        private int like;
        private int sad;
        private int best;
        private int funny;

        public ReactionCounts() {
            this.like = 0;
            this.sad = 0;
            this.best = 0;
            this.funny = 0;
        }

        public ReactionCounts(int like, int sad, int best, int funny) {
            this.like = like;
            this.sad = sad;
            this.best = best;
            this.funny = funny;
        }

        public int getLike() { return like; }
        public int getSad() { return sad; }
        public int getBest() { return best; }
        public int getFunny() { return funny; }
    }


    // ✅ Getter (Retrofit에서 필요 시 사용)
    public String getWriterKakaoId() { return writerKakaoId; }
    public String getMessage() { return message; }
    public Location getLocation() { return location; }
    public ReactionCounts getReactionCounts() { return reactionCounts; }
    public String getCreatedAt() { return createdAt; }
    public String getMyReaction() { return myReaction; }

}
