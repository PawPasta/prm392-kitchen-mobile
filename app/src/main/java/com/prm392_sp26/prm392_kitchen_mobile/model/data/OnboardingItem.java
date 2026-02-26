package com.prm392_sp26.prm392_kitchen_mobile.model.data;

/**
 * Data model cho mỗi trang onboarding.
 * Mỗi object OnboardingItem đại diện cho 1 trang (title + description).
 */
public class OnboardingItem {

    private String title;
    private String description;

    // Constructor - khởi tạo với title và description
    public OnboardingItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
