package com.example.np_team_k.ui.my.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.np_team_k.databinding.ItemStateHistoryBinding;
import com.example.np_team_k.network.StatusMineResponse;
import java.util.ArrayList;
import java.util.List;

public class MyStateHistoryAdapter extends RecyclerView.Adapter<MyStateHistoryAdapter.ViewHolder> {

    private List<StatusMineResponse> items = new ArrayList<>();

    // API에서 받아온 데이터 설정하는 메서드
    public void setData(List<StatusMineResponse> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemStateHistoryBinding binding = ItemStateHistoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStateHistoryBinding binding;

        public ViewHolder(ItemStateHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(StatusMineResponse item) {
            binding.setItem(item);  // item_state_history.xml에서 변수명 'item' 사용해야 함!
            binding.executePendingBindings();
        }
    }
}
