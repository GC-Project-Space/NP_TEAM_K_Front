package com.example.np_team_k.domain.model;

public class StatusResponse {

    private String id;
    private String writerKakaoId;
    private String message;
    private Location location;
    private int viewCount;
    private ReactionCounts reactionCounts;
    private String createdAt;
    private String myReaction;

    public String getMessage() {
        return message;
    }

    public Location getLocation() {
        return location;
    }

    public ReactionCounts getReactionCounts() {
        return reactionCounts;
    }

    public String getMyReaction() {
        return myReaction;
    }

    public static class Location {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public static class ReactionCounts {
        private int like;
        private int sad;
        private int best;
        private int funny;

        public int getLike() {
            return like;
        }

        public int getSad() {
            return sad;
        }

        public int getBest() {
            return best;
        }

        public int getFunny() {
            return funny;
        }
    }
}
