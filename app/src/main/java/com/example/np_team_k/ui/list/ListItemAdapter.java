package com.example.np_team_k.ui.list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.np_team_k.databinding.ItemListCardBinding;

public class ListItemAdapter extends ListAdapter<ListItem, ListItemAdapter.ViewHolder> {

    private final ReactionClickHandler clickHandler;

    public ListItemAdapter(ReactionClickHandler clickHandler) {
        super(DIFF_CALLBACK);
        this.clickHandler = clickHandler;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemListCardBinding binding;

        public ViewHolder(ItemListCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ListItem item, ReactionClickHandler handler) {
            binding.setItem(item);
            binding.setClickHandler(handler);
            binding.executePendingBindings();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemListCardBinding binding = ItemListCardBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), clickHandler);
    }

    private static final DiffUtil.ItemCallback<ListItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<ListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            return oldItem.getText().equals(newItem.getText()) &&
                    ((oldItem.getSelectedReaction() == null && newItem.getSelectedReaction() == null) ||
                            (oldItem.getSelectedReaction() != null &&
                                    oldItem.getSelectedReaction().equals(newItem.getSelectedReaction())));
        }
    };
}
