package com.example.np_team_k.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.np_team_k.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<ListItem> items;

    public ListAdapter(List<ListItem> items) {
        this.items = items;
    }

    public void setItems(List<ListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem item = items.get(position);

        holder.textContent.setText(item.getMessage());

        String distanceFormatted = String.format("%.0f m", item.getDistance());
        holder.textMeta.setText("관심도: " + item.getLikes() + " / 거리: " + distanceFormatted);

        holder.funnyIcon.setImageResource(item.isFunnySelected()
                ? R.drawable.ic_funny_full : R.drawable.ic_funny_empty);
        holder.heartIcon.setImageResource(item.isHeartSelected()
                ? R.drawable.ic_heart_full : R.drawable.ic_heart_empty);
        holder.thumbIcon.setImageResource(item.isThumbSelected()
                ? R.drawable.ic_thumb_full : R.drawable.ic_thumb_empty);
        holder.sadIcon.setImageResource(item.isSadSelected()
                ? R.drawable.ic_sad_full : R.drawable.ic_sad_empty);

        holder.funnyIcon.setOnClickListener(v -> {
            item.setFunnySelected(!item.isFunnySelected());
            item.setHeartSelected(false);
            item.setThumbSelected(false);
            item.setSadSelected(false);
            notifyItemChanged(position);
        });

        holder.heartIcon.setOnClickListener(v -> {
            item.setHeartSelected(!item.isHeartSelected());
            item.setFunnySelected(false);
            item.setThumbSelected(false);
            item.setSadSelected(false);
            notifyItemChanged(position);
        });

        holder.thumbIcon.setOnClickListener(v -> {
            item.setThumbSelected(!item.isThumbSelected());
            item.setFunnySelected(false);
            item.setHeartSelected(false);
            item.setSadSelected(false);
            notifyItemChanged(position);
        });

        holder.sadIcon.setOnClickListener(v -> {
            item.setSadSelected(!item.isSadSelected());
            item.setFunnySelected(false);
            item.setHeartSelected(false);
            item.setThumbSelected(false);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textContent;
        TextView textMeta;
        ImageView funnyIcon, heartIcon, thumbIcon, sadIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.text_content);
            textMeta = itemView.findViewById(R.id.text_meta);
            funnyIcon = itemView.findViewById(R.id.icon_funny);
            heartIcon = itemView.findViewById(R.id.icon_heart);
            thumbIcon = itemView.findViewById(R.id.icon_thumb);
            sadIcon = itemView.findViewById(R.id.icon_sad);
        }
    }
}
