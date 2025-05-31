package com.example.np_team_k.network;
public class EmotionResponse {

    private int total;
    private EmotionCounts emotionCounts;

    public int getTotal() { return total; }
    public EmotionCounts getEmotionCounts() { return emotionCounts; }

    public static class EmotionCounts {
        private int sad;
        private int anxious;
        private int happy;
        private int surprise;
        private int lonely;
        private int angry;

        public int getSad() { return sad; }
        public int getAnxious() { return anxious; }
        public int getHappy() { return happy; }
        public int getSurprise() { return surprise; }
        public int getLonely() { return lonely; }
        public int getAngry() { return angry; }
    }
}

