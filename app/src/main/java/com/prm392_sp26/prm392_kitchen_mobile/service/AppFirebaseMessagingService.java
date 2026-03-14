package com.prm392_sp26.prm392_kitchen_mobile.service;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prm392_sp26.prm392_kitchen_mobile.util.AppForegroundTracker;
import com.prm392_sp26.prm392_kitchen_mobile.util.InAppMessageHelper;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AppFcmService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String body = null;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        }

        Map<String, String> data = remoteMessage.getData();
        if (data != null && !data.isEmpty()) {
            if (title == null || title.trim().isEmpty()) {
                title = data.get("title");
            }
            if (body == null || body.trim().isEmpty()) {
                body = data.get("body");
                if (body == null || body.trim().isEmpty()) {
                    body = data.get("message");
                    if (body == null || body.trim().isEmpty()) {
                        body = data.get("inapp_message");
                    }
                }
            }
        }

        if ((title == null || title.trim().isEmpty()) && (body == null || body.trim().isEmpty())) {
            Log.d(TAG, "FCM message has no display content.");
            return;
        }

        if (AppForegroundTracker.isAppInForeground()) {
            Activity activity = AppForegroundTracker.getCurrentActivity();
            if (activity == null) {
                Log.d(TAG, "App in foreground but no active activity.");
                return;
            }
            String finalTitle = title;
            String finalBody = body;
            new Handler(Looper.getMainLooper()).post(
                    () -> InAppMessageHelper.show(activity, finalTitle, finalBody)
            );
        } else {
            Log.d(TAG, "App in background, skip in-app message.");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        PrefsManager.getInstance(getApplicationContext()).saveFcmToken(token);
    }
}
