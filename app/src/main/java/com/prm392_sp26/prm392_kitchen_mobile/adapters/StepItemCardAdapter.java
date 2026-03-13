package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.text.TextUtils;
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
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PlaceholderImageResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StepItemCardAdapter extends RecyclerView.Adapter<StepItemCardAdapter.StepItemViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemResponse item);
    }

    private final List<ItemResponse> items = new ArrayList<>();
    private final OnItemClickListener onItemClickListener;

    public StepItemCardAdapter() {
        this(null);
    }

    public StepItemCardAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setItems(List<ItemResponse> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StepItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new StepItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepItemViewHolder holder, int position) {
        ItemResponse item = items.get(position);

        holder.tvName.setText(nonEmpty(item.getName(), "Item"));
        holder.tvDescription.setText(nonEmpty(item.getDescription(), "Không có mô tả"));
        holder.tvPrice.setText(CurrencyFormatter.formatVnd(item.getPrice()));
        holder.tvInfo.setText(buildInfoText(item));

        String status = item.getStatus();
        boolean enabled = status == null || "ENABLE".equalsIgnoreCase(status.trim());
        holder.itemView.setAlpha(enabled ? 1.0f : 0.68f);

        String imageUrl = PlaceholderImageResolver.resolveItemImageUrl(item);
        holder.tvEmoji.setVisibility(View.GONE);
        holder.ivImage.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_dish)
                .error(R.drawable.ic_dish)
                .centerCrop()
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildInfoText(ItemResponse item) {
        String quantity = formatNumber(item.getBaseQuantity());
        String unit = item.getUnit() == null ? "" : item.getUnit().trim();
        String kcal = formatNumber(item.getCalories());
        return quantity + " " + unit + "  •  " + kcal + " kcal";
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String getItemEmoji(ItemResponse item) {
        String stepName = item.getStepName() == null ? "" : item.getStepName().toLowerCase(Locale.ROOT);
        if (stepName.contains("carb")) return "🍚";
        if (stepName.contains("protein")) return "🍗";
        if (stepName.contains("vegetable")) return "🥦";
        if (stepName.contains("sauce")) return "🥣";
        if (stepName.contains("extra")) return "✨";

        String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
        if (name.contains("rice")) return "🍚";
        if (name.contains("chicken") || name.contains("beef") || name.contains("salmon")) return "🍗";
        if (name.contains("sauce")) return "🥣";
        return "🍽️";
    }

    static class StepItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvEmoji;
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvInfo;
        private final TextView tvPrice;

        StepItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivDishImage);
            tvEmoji = itemView.findViewById(R.id.tvDishIcon);
            tvName = itemView.findViewById(R.id.tvDishName);
            tvDescription = itemView.findViewById(R.id.tvDishDescription);
            tvInfo = itemView.findViewById(R.id.tvDishInfo);
            tvPrice = itemView.findViewById(R.id.tvDishPrice);
        }
    }
}
