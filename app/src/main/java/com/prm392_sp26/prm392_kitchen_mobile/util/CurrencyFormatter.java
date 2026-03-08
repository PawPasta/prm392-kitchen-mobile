package com.prm392_sp26.prm392_kitchen_mobile.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {
    private static final Locale VIETNAM = new Locale("vi", "VN");

    private CurrencyFormatter() {
    }

    public static String formatVnd(double amount) {
        NumberFormat format = NumberFormat.getNumberInstance(VIETNAM);
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format.format(amount) + " VND";
    }
}
