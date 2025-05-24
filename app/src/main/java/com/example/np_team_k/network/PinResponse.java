package com.example.np_team_k.network;

import android.util.Log;

import java.util.List;

public class PinResponse {
    private List<Pin> pins;

    public List<Pin> getPins() {
        return pins;
    }

    public static class Pin {
        private String id;
        private String writerKakaoId;
        private String message;
        private double latitude;
        private double longitude;
        private ReactionCounts reactionCounts;
        private String createdAt;
        private String myReaction;

        public static class ReactionCounts {
            private int like;
            private int sad;
            private int best;
            private int funny;
            public int getLike() { return like; }
            public int getSad() { return sad; }
            public int getBest() { return best; }
            public int getFunny() { return funny; }
        }


        public  String getId() { return id; }

        public String getWriterKakaoId() {
            return writerKakaoId;
        }

        public String getMessage() {
            return message;
        }

        public double getLatitude() {
            Log.d("Pin", "Latitude: " + latitude);
            return latitude;
        }

        public double getLongitude() {
            Log.d("Pin", "Longitude: " + longitude);
            return longitude;
        }

        public ReactionCounts getReactionCounts() { return reactionCounts; }

        public String getCreatedAt() { return createdAt; }

        public String getMyReaction() { return myReaction; }
    }
}
