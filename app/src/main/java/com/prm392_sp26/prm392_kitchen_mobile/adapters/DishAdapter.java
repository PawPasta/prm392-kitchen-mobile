package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private List<DishResponse> dishes;
    private OnDishClickListener listener;

    // Interface để MainActivity bắt sự kiện click
    public interface OnDishClickListener {
        void onDishClick(DishResponse dish);
    }

    public DishAdapter(List<DishResponse> dishes, OnDishClickListener listener) {
        this.dishes = dishes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        DishResponse dish = dishes.get(position);

        holder.tvName.setText(dish.getName());

        // Format price in USD currency.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        holder.tvPrice.setText(nf.format(dish.getPrice()));

        // Hiện calories + status
        String statusEmoji = getStatusEmoji(dish.getStatus());
        holder.tvInfo.setText("🔥 " + (int) dish.getCalories() + " kcal  •  " + statusEmoji);

        String imageUrl = dish.getImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            holder.ivDishImage.setVisibility(View.GONE);
            holder.tvIcon.setVisibility(View.VISIBLE);
            holder.tvIcon.setText(getDishEmoji(dish.getName()));
        } else {
            holder.tvIcon.setVisibility(View.GONE);
            holder.ivDishImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView)
                .load(imageUrl.trim())
                .centerCrop()
                .into(holder.ivDishImage);
        }
        // Click vào món → gọi listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDishClick(dish);
        });
    }

    @Override
    public int getItemCount() {
        return dishes != null ? dishes.size() : 0;
    }

    // Cập nhật data khi API trả về
    public void updateDishes(List<DishResponse> newDishes) {
        this.dishes = newDishes;
        notifyDataSetChanged();
    }

    private String getStatusEmoji(String status) {
        if (status == null) return "";
        switch (status) {
            case "RECOMMENDED": return "⭐ Đề xuất";
            case "SEASONAL": return "🌸 Theo mùa";
            case "SPECIAL": return "🔥 Đặc biệt";
            default: return "";
        }
    }

    private String getDishEmoji(String name) {
        if (name == null) return "🍽️";
        String lower = name.toLowerCase();
        if (lower.contains("cơm") || lower.contains("com")) return "🍛";
        if (lower.contains("phở") || lower.contains("pho")) return "🍜";
        if (lower.contains("bánh mì") || lower.contains("banh mi")) return "🥖";
        if (lower.contains("trà") || lower.contains("tra")) return "🧋";
        if (lower.contains("nước") || lower.contains("nuoc")) return "🥤";
        if (lower.contains("gà") || lower.contains("ga")) return "🍗";
        return "🍽️";
    }

    static class DishViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivDishImage;
        final TextView tvIcon, tvName, tvInfo, tvPrice;

        DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivDishImage);
            tvIcon = itemView.findViewById(R.id.tvDishIcon);
            tvName = itemView.findViewById(R.id.tvDishName);
            tvInfo = itemView.findViewById(R.id.tvDishInfo);
            tvPrice = itemView.findViewById(R.id.tvDishPrice);
        }
    }
}
