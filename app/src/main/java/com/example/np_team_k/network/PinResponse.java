package com.example.np_team_k.network;

import android.util.Log;

import java.util.List;

public class PinResponse {
    private List<Pin> pins;

    public List<Pin> getPins() {
        return pins;
    }

    public static class Pin {
        private String writerKakaoId;
        private String message;
        private double latitude;
        private double longitude;

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
    }
}
