package com.example.np_team_k.ui.list;

public class ListItem {

    private String message;
    private int likes;
    private double distance;
    private String myReaction;

    private boolean funnySelected;
    private boolean heartSelected;
    private boolean thumbSelected;
    private boolean sadSelected;

    // 기존 3개짜리 생성자
    public ListItem(String message, int likes, double distance) {
        this.message = message;
        this.likes = likes;
        this.distance = distance;
        this.myReaction = null;

        this.funnySelected = false;
        this.heartSelected = false;
        this.thumbSelected = false;
        this.sadSelected = false;
    }

    // ✅ 새로 추가된 4개짜리 생성자
    public ListItem(String message, int likes, double distance, String myReaction) {
        this.message = message;
        this.likes = likes;
        this.distance = distance;
        this.myReaction = myReaction;

        this.funnySelected = "funny".equals(myReaction);
        this.heartSelected = "heart".equals(myReaction);
        this.thumbSelected = "thumb".equals(myReaction);
        this.sadSelected = "sad".equals(myReaction);
    }

    public String getMessage() {
        return message;
    }

    public int getLikes() {
        return likes;
    }

    public double getDistance() {
        return distance;
    }

    public String getMyReaction() {
        return myReaction;
    }

    public void setMyReaction(String myReaction) {
        this.myReaction = myReaction;
    }

    public boolean isFunnySelected() {
        return funnySelected;
    }

    public void setFunnySelected(boolean funnySelected) {
        this.funnySelected = funnySelected;
    }

    public boolean isHeartSelected() {
        return heartSelected;
    }

    public void setHeartSelected(boolean heartSelected) {
        this.heartSelected = heartSelected;
    }

    public boolean isThumbSelected() {
        return thumbSelected;
    }

    public void setThumbSelected(boolean thumbSelected) {
        this.thumbSelected = thumbSelected;
    }

    public boolean isSadSelected() {
        return sadSelected;
    }

    public void setSadSelected(boolean sadSelected) {
        this.sadSelected = sadSelected;
    }
}
