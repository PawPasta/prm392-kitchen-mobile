package com.prm392_sp26.prm392_kitchen_mobile.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.prm392_sp26.prm392_kitchen_mobile.activities.AuthActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SessionManager {

    private static final AtomicBoolean LOGOUT_IN_PROGRESS = new AtomicBoolean(false);

    private SessionManager() {
    }

    public static void handleUnauthorized(Context context) {
        if (context == null) {
            return;
        }

        PrefsManager prefsManager = PrefsManager.getInstance(context);
        if (!prefsManager.isLoggedIn()) {
            return;
        }

        if (!LOGOUT_IN_PROGRESS.compareAndSet(false, true)) {
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            prefsManager.clearSession();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(context, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Toast.makeText(context,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                    Toast.LENGTH_LONG).show();

            LOGOUT_IN_PROGRESS.set(false);
        });
    }
}
