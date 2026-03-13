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
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;
import com.prm392_sp26.prm392_kitchen_mobile.util.PlaceholderImageResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_DISH = 1;

    public interface OnDishClickListener {
        void onDishClick(DishResponse dish);
    }

    private static class RowItem {
        final int type;
        final String title;
        final DishResponse dish;

        RowItem(int type, String title, DishResponse dish) {
            this.type = type;
            this.title = title;
            this.dish = dish;
        }
    }

    private final List<RowItem> rows = new ArrayList<>();
    private final OnDishClickListener listener;

    public MenuSectionAdapter(OnDishClickListener listener) {
        this.listener = listener;
    }

    public void setDishes(List<DishResponse> dishes) {
        rows.clear();
        if (dishes == null || dishes.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Map<String, List<DishResponse>> grouped = new LinkedHashMap<>();
        grouped.put("⭐ Đề xuất", new ArrayList<>());
        grouped.put("🔥 Đặc biệt", new ArrayList<>());
        grouped.put("🌸 Theo mùa", new ArrayList<>());
        grouped.put("Khác", new ArrayList<>());

        for (DishResponse dish : dishes) {
            String label = getStatusLabel(dish.getStatus());
            if (!grouped.containsKey(label)) {
                grouped.put(label, new ArrayList<>());
            }
            grouped.get(label).add(dish);
        }

        for (Map.Entry<String, List<DishResponse>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            rows.add(new RowItem(VIEW_TYPE_HEADER, entry.getKey(), null));
            for (DishResponse dish : entry.getValue()) {
                rows.add(new RowItem(VIEW_TYPE_DISH, null, dish));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_menu_section_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_dish_grid, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowItem item = rows.get(position);
        if (item.type == VIEW_TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.title);
        } else if (item.dish != null) {
            ((DishViewHolder) holder).bind(item.dish, listener);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Khác";
        switch (status) {
            case "RECOMMENDED": return "⭐ Đề xuất";
            case "SEASONAL": return "🌸 Theo mùa";
            case "SPECIAL": return "🔥 Đặc biệt";
            default: return "Khác";
        }
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
        String lower = name.toLowerCase(Locale.US);
        if (lower.contains("cơm") || lower.contains("com")) return "🍛";
        if (lower.contains("phở") || lower.contains("pho")) return "🍜";
        if (lower.contains("bánh mì") || lower.contains("banh mi")) return "🥖";
        if (lower.contains("trà") || lower.contains("tra")) return "🧋";
        if (lower.contains("nước") || lower.contains("nuoc")) return "🥤";
        if (lower.contains("gà") || lower.contains("ga")) return "🍗";
        return "🍽️";
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSectionTitle);
        }

        void bind(String title) {
            tvTitle.setText(title);
        }
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvIcon;
        private final TextView tvName;
        private final TextView tvInfo;
        private final TextView tvPrice;

        DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivDishImage);
            tvIcon = itemView.findViewById(R.id.tvDishIcon);
            tvName = itemView.findViewById(R.id.tvDishName);
            tvInfo = itemView.findViewById(R.id.tvDishInfo);
            tvPrice = itemView.findViewById(R.id.tvDishPrice);
        }

        void bind(DishResponse dish, OnDishClickListener listener) {
            tvName.setText(dish.getName());
            tvPrice.setText(CurrencyFormatter.formatVnd(dish.getPrice()));

            String statusEmoji = getStatusEmoji(dish.getStatus());
            String info = "🔥 " + (int) dish.getCalories() + " kcal";
            if (!statusEmoji.isEmpty()) {
                info += "  •  " + statusEmoji;
            }
            tvInfo.setText(info);

            String imageUrl = PlaceholderImageResolver.resolveDishImageUrl(dish.getImageUrl());
            tvIcon.setVisibility(View.GONE);
            ivDishImage.setVisibility(View.VISIBLE);
            Glide.with(itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_dish)
                .error(R.drawable.ic_dish)
                .centerCrop()
                .into(ivDishImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDishClick(dish);
                }
            });
        }
    }
}
