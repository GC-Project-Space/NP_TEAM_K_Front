package com.example.np_team_k.network;

import java.util.Map;

public class ReactionResponse {
    private int total;
    private Map<String, Integer> receivedReactions;

    public int getTotal() {
        return total;
    }

    public Map<String, Integer> getReceivedReactions() {
        return receivedReactions;
    }
}
