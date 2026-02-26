package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.data.OnboardingItem;

import java.util.List;



public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    // Danh sách 3 trang onboarding
    private final List<OnboardingItem> items;

    // Mảng emoji cho 3 trang
    private final String[] emojis = {"🍳", "📱", "🚀"};

    // Constructor - nhận vào list data
    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    
    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    
    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);

        // Set emoji theo position (0=🍳, 1=📱, 2=🚀)
        holder.tvIcon.setText(emojis[position]);
        // Set tiêu đề và mô tả từ OnboardingItem
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
    }

   
    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {

        final TextView tvIcon;
        final TextView tvTitle;
        final TextView tvDesc;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Tìm các view 1 lần duy nhất, lưu vào biến
            tvIcon = itemView.findViewById(R.id.tvOnboardingIcon);
            tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
            tvDesc = itemView.findViewById(R.id.tvOnboardingDesc);
        }
    }
}
