package com.prm392_sp26.prm392_kitchen_mobile.util;

import com.prm392_sp26.prm392_kitchen_mobile.model.response.ItemResponse;

import java.util.Locale;

public final class PlaceholderImageResolver {

    private static final String DISH_FALLBACK_URL =
            "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg";

    private static final String ITEM_CARB_FALLBACK_URL =
            "https://images.pexels.com/photos/723198/pexels-photo-723198.jpeg";
    private static final String ITEM_PROTEIN_FALLBACK_URL =
            "https://images.pexels.com/photos/616354/pexels-photo-616354.jpeg";
    private static final String ITEM_VEGETABLE_FALLBACK_URL =
            "https://images.pexels.com/photos/1656666/pexels-photo-1656666.jpeg";
    private static final String ITEM_SAUCE_FALLBACK_URL =
            "https://images.pexels.com/photos/1435895/pexels-photo-1435895.jpeg";
    private static final String ITEM_EXTRA_FALLBACK_URL =
            "https://images.pexels.com/photos/557659/pexels-photo-557659.jpeg";

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
