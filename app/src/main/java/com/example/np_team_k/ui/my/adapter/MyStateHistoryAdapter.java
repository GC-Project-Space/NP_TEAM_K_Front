package com.example.np_team_k.ui.my.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.np_team_k.databinding.ItemStateHistoryBinding;
import com.example.np_team_k.ui.my.model.MyStateItem;

import java.util.List;

public class MyStateHistoryAdapter extends RecyclerView.Adapter<MyStateHistoryAdapter.ViewHolder> {

    private final List<MyStateItem> items;

    public MyStateHistoryAdapter(List<MyStateItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStateHistoryBinding binding;

        public ViewHolder(ItemStateHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MyStateItem item) {
            binding.setItem(item); // 자동으로 text에 들어감
            binding.executePendingBindings(); // 즉시 바인딩
        }
    }

    @NonNull
    @Override
    public MyStateHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemStateHistoryBinding binding = ItemStateHistoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyStateHistoryAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
