package com.example.np_team_k.network;

import com.google.gson.annotations.SerializedName;

public class WeeklyActivityResponse {

    @SerializedName("월")
    private int mon;

    @SerializedName("화")
    private int tue;

    @SerializedName("수")
    private int wed;

    @SerializedName("목")
    private int thu;

    @SerializedName("금")
    private int fri;

    @SerializedName("토")
    private int sat;

    @SerializedName("일")
    private int sun;

    // Getter & Setter methods
    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }
}

