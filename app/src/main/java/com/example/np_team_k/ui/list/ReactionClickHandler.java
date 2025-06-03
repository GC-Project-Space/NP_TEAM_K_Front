package com.example.np_team_k.ui.list;

public class ReactionClickHandler {
    private final ListPageViewModel viewModel;

    public ReactionClickHandler(ListPageViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void onReactionClick(String id, String type) {
        viewModel.updateReaction(id, type);
    }
}

