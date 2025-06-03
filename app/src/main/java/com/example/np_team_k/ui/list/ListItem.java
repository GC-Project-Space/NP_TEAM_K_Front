package com.example.np_team_k.ui.list;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.np_team_k.BR;

public class ListItem extends BaseObservable {

    private final String id;
    private String text;
    private String selectedReaction;

    public ListItem(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    @Bindable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public String getSelectedReaction() {
        return selectedReaction;
    }

    public void setSelectedReaction(String selectedReaction) {
        this.selectedReaction = selectedReaction;
        notifyPropertyChanged(BR.selectedReaction);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ListItem other = (ListItem) obj;
        return id.equals(other.id) &&
                text.equals(other.text) &&
                ((selectedReaction == null && other.selectedReaction == null) ||
                        (selectedReaction != null && selectedReaction.equals(other.selectedReaction)));
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + (selectedReaction != null ? selectedReaction.hashCode() : 0);
        return result;
    }


}


