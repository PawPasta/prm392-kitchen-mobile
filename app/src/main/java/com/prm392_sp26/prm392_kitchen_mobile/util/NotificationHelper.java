package com.prm392_sp26.prm392_kitchen_mobile.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.prm392_sp26.prm392_kitchen_mobile.R;
import com.prm392_sp26.prm392_kitchen_mobile.activities.NotificationActivity;
import com.prm392_sp26.prm392_kitchen_mobile.activities.NotificationDetailActivity;

import java.util.Map;

public final class NotificationHelper {

    public static final String CHANNEL_ID = "kitchen_updates";
    private static final String CHANNEL_NAME = "Kitchen updates";
    private static final String CHANNEL_DESC = "Order and account notifications";
    private static final String DATA_NOTIFICATION_ID = "notification_id";
    private static final String DATA_NOTIFICATION_ID_CAMEL = "notificationId";

    private NotificationHelper() {
    }

    public static void createNotificationChannel(Context context) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(CHANNEL_DESC);
        manager.createNotificationChannel(channel);
    }

    public static boolean canPostNotifications(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void showNotification(Context context, String title, String body, Map<String, String> data) {
        if (context == null || !canPostNotifications(context)) {
            return;
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return;
        }

        createNotificationChannel(context);

        String safeTitle = title == null || title.trim().isEmpty()
                ? context.getString(R.string.app_name)
                : title.trim();
        String safeBody = body == null ? "" : body.trim();

        PendingIntent pendingIntent = buildPendingIntent(context, data);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(safeTitle)
                .setContentText(safeBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeBody))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        int notificationId = (int) (System.currentTimeMillis() & 0x7fffffff);
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Permission can be revoked while the app is running.
        }
    }

    private static PendingIntent buildPendingIntent(Context context, Map<String, String> data) {
        long notificationId = parseNotificationId(data);
        Intent intent;
        if (notificationId > 0) {
            intent = new Intent(context, NotificationDetailActivity.class);
            intent.putExtra(NotificationDetailActivity.EXTRA_NOTIFICATION_ID, notificationId);
        } else {
            intent = new Intent(context, NotificationActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (int) (System.currentTimeMillis() & 0x7fffffff);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static long parseNotificationId(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return -1;
        }
        String raw = data.get(DATA_NOTIFICATION_ID);
        if (raw == null || raw.trim().isEmpty()) {
            raw = data.get(DATA_NOTIFICATION_ID_CAMEL);
        }
        if (raw == null || raw.trim().isEmpty()) {
            return -1;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
