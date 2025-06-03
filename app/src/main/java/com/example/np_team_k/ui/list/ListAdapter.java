package com.example.np_team_k.ui.list;

import android.util.Log;
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

    private List<ListItem> itemList;

    public ListAdapter(List<ListItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem item = itemList.get(position);
        holder.textView.setText(item.getText());

        updateReactionIcons(holder, item.getSelectedReaction());

        holder.heartIcon.setOnClickListener(v -> {
            toggleReaction(item, "heart");
            notifyItemChanged(position);
        });

        holder.funnyIcon.setOnClickListener(v -> {
            toggleReaction(item, "funny");
            notifyItemChanged(position);
        });

        holder.thumbIcon.setOnClickListener(v -> {
            toggleReaction(item, "thumb");
            notifyItemChanged(position);
        });

        holder.sadIcon.setOnClickListener(v -> {
            toggleReaction(item, "sad");
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView heartIcon, funnyIcon, thumbIcon, sadIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_content);
            heartIcon = itemView.findViewById(R.id.icon_heart);
            funnyIcon = itemView.findViewById(R.id.icon_funny);
            thumbIcon = itemView.findViewById(R.id.icon_thumb);
            sadIcon = itemView.findViewById(R.id.icon_sad);

            if (heartIcon == null) Log.e("ListAdapter", "heartIcon is null");
            if (funnyIcon == null) Log.e("ListAdapter", "funnyIcon is null");
            if (thumbIcon == null) Log.e("ListAdapter", "thumbIcon is null");
            if (sadIcon == null) Log.e("ListAdapter", "sadIcon is null");
        }
    }

    private void toggleReaction(ListItem item, String clicked) {
        if (clicked.equals(item.getSelectedReaction())) {
            item.setSelectedReaction(null);
        } else {
            item.setSelectedReaction(clicked);
        }
    }

    private void updateReactionIcons(ViewHolder holder, String selected) {
        holder.heartIcon.setImageResource("heart".equals(selected) ? R.drawable.ic_heart_full : R.drawable.ic_heart_empty);
        holder.funnyIcon.setImageResource("funny".equals(selected) ? R.drawable.ic_funny_full : R.drawable.ic_funny_empty);
        holder.thumbIcon.setImageResource("thumb".equals(selected) ? R.drawable.ic_thumb_full : R.drawable.ic_thumb_empty);
        holder.sadIcon.setImageResource("sad".equals(selected) ? R.drawable.ic_sad_full : R.drawable.ic_sad_empty);
    }
}
