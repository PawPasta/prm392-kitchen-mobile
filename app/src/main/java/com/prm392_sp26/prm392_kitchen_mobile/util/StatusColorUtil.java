package com.prm392_sp26.prm392_kitchen_mobile.util;

import com.prm392_sp26.prm392_kitchen_mobile.R;

import java.util.Locale;

public final class StatusColorUtil {

    private StatusColorUtil() {
    }

    public static int getStatusColorRes(String status) {
        if (status == null) {
            return R.color.colorTextSecondary;
        }
        switch (status.trim().toUpperCase(Locale.US)) {
            case "CREATED":
                return R.color.statusBlue;
            case "CONFIRMED":
                return R.color.statusBlue;
            case "PROCESSING":
                return R.color.statusYellow;
            case "READY":
                return R.color.statusPink;
            case "COMPLETED":
                return R.color.statusGreen;
            case "CANCELLED":
                return R.color.statusRed;
            default:
                return R.color.colorTextSecondary;
        }
    }

    public static int getStatusBackgroundColorRes(String status) {
        if (status == null) {
            return R.color.colorSurfaceVariant;
        }
        switch (status.trim().toUpperCase(Locale.US)) {
            case "CREATED":
                return R.color.statusBlue;
            case "CONFIRMED":
                return R.color.statusBlue;
            case "PROCESSING":
                return R.color.statusYellow;
            case "READY":
                return R.color.statusPink;
            case "COMPLETED":
                return R.color.statusGreen;
            case "CANCELLED":
                return R.color.statusRed;
            default:
                return R.color.colorSurfaceVariant;
        }
    }
}
