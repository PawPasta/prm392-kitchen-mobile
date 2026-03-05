package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.OrderHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter để hiển thị danh sách order
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderHistoryResponse.OrderItem> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(OrderHistoryResponse.OrderItem item);
    }

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (position < items.size()) {
            holder.bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvOrderStatus;
        private final TextView tvOrderCreatedAt;
        private final TextView tvDishCount;
        private final TextView tvPickupTime;
        private final TextView tvOrderTotal;
        private final TextView tvOrderDiscount;
        private final TextView tvOrderFinalAmount;
        private final TextView tvOrderNote;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderCreatedAt = itemView.findViewById(R.id.tvOrderCreatedAt);
            tvDishCount = itemView.findViewById(R.id.tvDishCount);
            tvPickupTime = itemView.findViewById(R.id.tvPickupTime);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderDiscount = itemView.findViewById(R.id.tvOrderDiscount);
            tvOrderFinalAmount = itemView.findViewById(R.id.tvOrderFinalAmount);
            tvOrderNote = itemView.findViewById(R.id.tvOrderNote);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(items.get(position));
                }
            });
        }

        void bind(OrderHistoryResponse.OrderItem order) {
            // Order ID
            String orderId = order.getOrderId();
            String shortId = orderId != null && orderId.length() >= 8 ? orderId.substring(0, 8) : orderId;
            tvOrderId.setText(String.format(Locale.US, "Order #%s", shortId));

            // Status
            String status = getStatusLabel(order.getStatus());
            tvOrderStatus.setText(status);
            tvOrderStatus.setBackgroundColor(getStatusColor(order.getStatus()));

            // Created At
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                String createdAt = order.getCreatedAt();
                if (createdAt != null) {
                    java.util.Date parsed = isoFormat.parse(createdAt.substring(0, 19));
                    if (parsed != null) {
                        tvOrderCreatedAt.setText(displayFormat.format(parsed));
                    }
                }
            } catch (Exception e) {
                tvOrderCreatedAt.setText(order.getCreatedAt());
            }

            // Dishes count and total quantity
            if (order.getDishes() != null && !order.getDishes().isEmpty()) {
                int totalQuantity = 0;
                for (OrderHistoryResponse.OrderDish dish : order.getDishes()) {
                    totalQuantity += dish.getQuantity();
                }
                tvDishCount.setText(String.format(Locale.US, "%d món • Số lượng: %d", order.getDishes().size(), totalQuantity));
            }

            // Pickup time
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                String pickupAt = order.getPickupAt();
                if (pickupAt != null) {
                    java.util.Date parsed = isoFormat.parse(pickupAt.substring(0, 19));
                    if (parsed != null) {
                        tvPickupTime.setText(itemView.getContext().getString(R.string.pickup_label, displayFormat.format(parsed)));
                    }
                }
            } catch (Exception e) {
                tvPickupTime.setText(order.getPickupAt());
            }

            // Prices
            tvOrderTotal.setText(String.format(Locale.US, "%.2f", order.getTotalPrice()));
            tvOrderFinalAmount.setText(String.format(Locale.US, "%.2f", order.getFinalAmount()));

            // Discount
            if (order.getDiscountAmount() > 0) {
                tvOrderDiscount.setText(String.format(Locale.US, "-$%.2f", order.getDiscountAmount()));
                tvOrderDiscount.setVisibility(View.VISIBLE);
            } else {
                tvOrderDiscount.setVisibility(View.GONE);
            }

            // Note
            if (order.getNote() != null && !order.getNote().isEmpty()) {
                tvOrderNote.setVisibility(View.VISIBLE);
            } else {
                tvOrderNote.setVisibility(View.GONE);
            }
        }

        private String getStatusLabel(String status) {
            if (status == null) return "N/A";
            if ("CREATED".equals(status)) return "Đã tạo";
            if ("CONFIRMED".equals(status)) return "Đã xác nhận";
            if ("PROCESSING".equals(status)) return "Đang chuẩn bị";
            if ("READY".equals(status)) return "Sẵn sàng";
            if ("COMPLETED".equals(status)) return "Hoàn thành";
            if ("CANCELLED".equals(status)) return "Đã hủy";
            return status;
        }

        private int getStatusColor(String status) {
            if (status == null) return itemView.getContext().getColor(R.color.colorSurfaceVariant);
            if ("CREATED".equals(status)) return itemView.getContext().getColor(R.color.colorSurfaceVariant);
            if ("CONFIRMED".equals(status)) return itemView.getContext().getColor(R.color.colorPrimaryLight);
            if ("PROCESSING".equals(status)) return itemView.getContext().getColor(R.color.colorPrimary);
            if ("READY".equals(status)) return itemView.getContext().getColor(R.color.colorSuccess);
            if ("COMPLETED".equals(status)) return itemView.getContext().getColor(R.color.colorSuccess);
            if ("CANCELLED".equals(status)) return itemView.getContext().getColor(R.color.colorError);
            return itemView.getContext().getColor(R.color.colorSurfaceVariant);
        }
    }
}




