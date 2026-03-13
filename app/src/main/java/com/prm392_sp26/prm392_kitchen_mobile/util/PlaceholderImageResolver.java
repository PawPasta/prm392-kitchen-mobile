package com.prm392_sp26.prm392_kitchen_mobile.util;

import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.util.Locale;

public final class PlaceholderImageResolver {

    private static final String DISH_FALLBACK_URL =
            "https://placehold.co/1200x800/FFE7D6/E55A2B?text=Dish";

    private static final String ITEM_CARB_FALLBACK_URL =
            "https://placehold.co/1200x800/FDE68A/7C2D12?text=Carb";
    private static final String ITEM_PROTEIN_FALLBACK_URL =
            "https://placehold.co/1200x800/FCA5A5/7F1D1D?text=Protein";
    private static final String ITEM_VEGETABLE_FALLBACK_URL =
            "https://placehold.co/1200x800/BBF7D0/14532D?text=Vegetables";
    private static final String ITEM_SAUCE_FALLBACK_URL =
            "https://placehold.co/1200x800/BFDBFE/1E3A8A?text=Sauce";
    private static final String ITEM_EXTRA_FALLBACK_URL =
            "https://placehold.co/1200x800/E9D5FF/581C87?text=Extra";

    private PlaceholderImageResolver() {
    }

    public static String resolveDishImageUrl(String imageUrl) {
        String normalized = normalize(imageUrl);
        if (!normalized.isEmpty()) {
            return normalized;
        }
        return DISH_FALLBACK_URL;
    }

    public static String resolveItemImageUrl(ItemResponse item) {
        if (item == null) {
            return ITEM_CARB_FALLBACK_URL;
        }
        return resolveItemImageUrl(item.getImageUrl(), item.getStepId(), item.getStepName());
    }

    public static String resolveItemImageUrl(String imageUrl, int stepId, String stepName) {
        String normalized = normalize(imageUrl);
        if (!normalized.isEmpty()) {
            return normalized;
        }

        int resolvedStep = resolveStep(stepId, stepName);
        switch (resolvedStep) {
            case 2:
                return ITEM_PROTEIN_FALLBACK_URL;
            case 3:
                return ITEM_VEGETABLE_FALLBACK_URL;
            case 4:
                return ITEM_SAUCE_FALLBACK_URL;
            case 5:
                return ITEM_EXTRA_FALLBACK_URL;
            case 1:
            default:
                return ITEM_CARB_FALLBACK_URL;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static int resolveStep(int stepId, String stepName) {
        if (stepId >= 1 && stepId <= 5) {
            return stepId;
        }
        String text = normalize(stepName).toLowerCase(Locale.ROOT);
        if (text.contains("carb")) {
            return 1;
        }
        if (text.contains("protein")) {
            return 2;
        }
        if (text.contains("vegetable")) {
            return 3;
        }
        if (text.contains("sauce")) {
            return 4;
        }
        if (text.contains("extra")) {
            return 5;
        }
        return 1;
    }
}
