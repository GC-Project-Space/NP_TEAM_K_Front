package com.example.np_team_k.network;

public class PinRequest {
    private String writerKakaoId;
    private String message;
    private Location location;

    public PinRequest(String writerKakaoId, String message, double latitude, double longitude) {
        this.writerKakaoId = writerKakaoId;
        this.message = message;
        this.location = new Location(latitude, longitude);
    }

    public static class Location {
        private double latitude;
        private double longitude;

        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
