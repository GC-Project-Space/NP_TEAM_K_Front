package com.example.np_team_k.ui.list;

public class ListItem {
    private String text;
    private String selectedReaction; // "heart", "funny", "thumb", "sad" or null

    public ListItem(String text) {
        this.text = text;
        this.selectedReaction = null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSelectedReaction() {
        return selectedReaction;
    }

    public void setSelectedReaction(String selectedReaction) {
        this.selectedReaction = selectedReaction;
    }
}


