package com.example.np_team_k.network;

import com.google.gson.annotations.SerializedName;

public class StatusMineResponse {

    @SerializedName("_id")
    private String id;

    private String nickname;
    private String message;
    private String emoji;

    private Location location;

    @SerializedName("createdAt")
    private String createdAt;

    // 내부 클래스: Location
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

    // Getter
    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public String getEmoji() {
        return emoji;
    }

    public Location getLocation() {
        return location;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
