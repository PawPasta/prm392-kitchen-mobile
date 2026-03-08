package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;
import com.prm392_sp26.prm392_kitchen_mobile.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<OrderHistoryResponse.OrderItem> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public void setItems(List<OrderHistoryResponse.OrderItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<OrderHistoryResponse.OrderItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderHistoryResponse.OrderItem item = items.get(position);
        String orderId = item.getOrderId() == null ? "" : item.getOrderId();
        holder.tvOrderId.setText("Order #" + shortId(orderId));
        holder.tvStatus.setText(item.getStatus() == null ? "" : item.getStatus());

        holder.tvCreatedAt.setText("Ngày: " + formatDateTime(item.getCreatedAt()));
        String pickup = item.getPickupAt();
        if (pickup == null || pickup.trim().isEmpty()) {
            holder.tvPickupAt.setVisibility(View.GONE);
        } else {
            holder.tvPickupAt.setVisibility(View.VISIBLE);
            holder.tvPickupAt.setText("Nhận: " + formatDateTime(pickup));
        }

        int totalQty = sumQuantity(item.getDishes());
        holder.tvItemsCount.setText("Món: " + totalQty);

        double amount = item.getFinalAmount() > 0 ? item.getFinalAmount() : item.getTotalPrice();
        holder.tvTotal.setText("Tổng: " + formatCurrency(amount));

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

    private int sumQuantity(List<OrderHistoryResponse.OrderDish> dishes) {
        int total = 0;
        if (dishes == null) {
            return total;
        }
        for (OrderHistoryResponse.OrderDish dish : dishes) {
            total += dish == null ? 0 : dish.getQuantity();
        }
        return total;
    }

    private String shortId(String id) {
        if (id == null) {
            return "";
        }
        String trimmed = id.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }
        return trimmed.substring(trimmed.length() - 8);
    }

    private String formatDateTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "--";
        }
        Date date = parseIsoDate(input);
        if (date == null) {
            return input.replace("T", " ").replace("Z", "");
        }
        SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return out.format(date);
    }

    private Date parseIsoDate(String input) {
        String value = input.trim();
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String formatCurrency(double amount) {
        return CurrencyFormatter.formatVnd(amount);
    }

    public interface OnItemClickListener {
        void onItemClick(OrderHistoryResponse.OrderItem item);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvStatus;
        private final TextView tvCreatedAt;
        private final TextView tvPickupAt;
        private final TextView tvItemsCount;
        private final TextView tvTotal;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvPickupAt = itemView.findViewById(R.id.tvPickupAt);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}
