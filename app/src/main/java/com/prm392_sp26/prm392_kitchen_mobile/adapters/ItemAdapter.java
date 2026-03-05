package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<ItemResponse> items;

    public ItemAdapter(List<ItemResponse> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemResponse item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCalories.setText((int) item.getCalories() + " kcal");

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvPrice.setText(nf.format(item.getPrice()));

        String imageUrl = item.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            holder.ivItemImage.setImageDrawable(null);
        } else {
            Glide.with(holder.itemView)
                .load(imageUrl.trim())
                .centerCrop()
                .into(holder.ivItemImage);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvName, tvCalories, tvPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvCalories = itemView.findViewById(R.id.tvItemCalories);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
        }
    }
}
