package com.prm392_sp26.prm392_kitchen_mobile;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prm392_sp26.prm392_kitchen_mobile.util.PrefsManager;

public class KitchenApplication extends Application {

    private static final String TAG = "KitchenApplication";
    private static KitchenApplication instance;

    public static KitchenApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        fetchAndStoreFcmTokenIfNeeded();
    }

    private void fetchAndStoreFcmTokenIfNeeded() {
        PrefsManager prefsManager = PrefsManager.getInstance(this);
        String accessToken = prefsManager.getAccessToken();
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Failed to fetch FCM token", task.getException());
                        return;
                    }
                    String fcmToken = task.getResult();
                    if (fcmToken == null || fcmToken.trim().isEmpty()) {
                        return;
                    }
                    prefsManager.saveFcmToken(fcmToken);
                });
    }
}
