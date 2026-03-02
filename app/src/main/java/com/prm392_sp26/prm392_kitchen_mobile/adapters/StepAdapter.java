package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.DishStepResponse;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private List<DishStepResponse> steps;
    private Map<Integer, List<ItemResponse>> itemsMap;

    public StepAdapter(List<DishStepResponse> steps) {
        this.steps = steps;
        this.itemsMap = new HashMap<>();
    }

    public void setItemsForStep(int stepId, List<ItemResponse> items) {
        itemsMap.put(stepId, items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        DishStepResponse step = steps.get(position);

        holder.tvStepNumber.setText(String.valueOf(step.getStepOrder()));
        holder.tvStepName.setText(step.getStepName());
        holder.tvStepRequired.setText(step.isRequired() ? "⚡ Bắt buộc" : "Tùy chọn");
        holder.tvStepDescription.setText(
            "Chọn " + step.getMinSelect() + " - " + step.getMaxSelect() + " mục"
        );

        List<ItemResponse> items = itemsMap.get(step.getStepId());
        if (items != null && !items.isEmpty()) {
            ItemAdapter itemAdapter = new ItemAdapter(items, step.getMaxSelect());
            holder.rvItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.rvItems.setAdapter(itemAdapter);
            holder.rvItems.setVisibility(View.VISIBLE);
        } else {
            holder.rvItems.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepName, tvStepRequired, tvStepDescription;
        RecyclerView rvItems;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepName = itemView.findViewById(R.id.tvStepName);
            tvStepRequired = itemView.findViewById(R.id.tvStepRequired);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            rvItems = itemView.findViewById(R.id.rvItems);
        }
    }
}
