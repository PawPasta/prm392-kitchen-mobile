package com.prm392_sp26.prm392_kitchen_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.model.response.NotificationItem;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationItem> notifications;
    private OnItemClickListener onItemClickListener;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM")
            .withLocale(Locale.US);

    public interface OnItemClickListener {
        void onItemClick(NotificationItem item);
    }

    public NotificationAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        String title = item.getTitle();
        String type = item.getType();

        holder.tvTitle.setText(title == null || title.trim().isEmpty() ? "Thông báo" : title);
        holder.tvType.setText(type == null || type.trim().isEmpty() ? "Khác" : type.trim());
        holder.tvTime.setText(formatTime(item.getCreatedAt()));

        holder.unreadDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    private String formatTime(String isoTime) {
        if (isoTime == null || isoTime.trim().isEmpty()) {
            return "--";
        }
        try {
            Instant instant = Instant.parse(isoTime.trim());
            return timeFormatter.format(instant.atZone(ZoneId.systemDefault()));
        } catch (Exception e) {
            return isoTime;
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvType;
        private final TextView tvTime;
        private final View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvType = itemView.findViewById(R.id.tvNotificationType);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            unreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
