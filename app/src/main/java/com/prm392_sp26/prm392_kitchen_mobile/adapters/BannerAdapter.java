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
import com.prm392_sp26.prm392_kitchen_mobile.model.data.BannerItem;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final List<BannerItem> items;

    public BannerAdapter(List<BannerItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getSubtitle());
        holder.tvCta.setText(item.getCta());
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.bg_gradient_primary)
                .error(R.drawable.bg_gradient_primary)
                .centerCrop()
                .into(holder.ivBannerBackground);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvCta;
        private final ImageView ivBannerBackground;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBannerBackground = itemView.findViewById(R.id.ivBannerBackground);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
            tvSubtitle = itemView.findViewById(R.id.tvBannerSubtitle);
            tvCta = itemView.findViewById(R.id.tvBannerCta);
        }
    }
}
