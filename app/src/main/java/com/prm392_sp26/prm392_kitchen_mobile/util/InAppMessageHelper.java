package com.prm392_sp26.prm392_kitchen_mobile.util;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public final class InAppMessageHelper {

    private InAppMessageHelper() {
    }

    public static void show(Activity activity, String title, String body) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if ((title == null || title.trim().isEmpty()) && (body == null || body.trim().isEmpty())) {
            return;
        }

        String message;
        if (title != null && !title.trim().isEmpty() && body != null && !body.trim().isEmpty()) {
            message = title.trim() + ": " + body.trim();
        } else if (body != null && !body.trim().isEmpty()) {
            message = body.trim();
        } else {
            message = title.trim();
        }

        View root = activity.findViewById(android.R.id.content);
        if (root == null) {
            return;
        }
        Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }
}
