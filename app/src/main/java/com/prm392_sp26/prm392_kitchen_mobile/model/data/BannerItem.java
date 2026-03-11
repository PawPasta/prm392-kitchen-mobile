package com.prm392_sp26.prm392_kitchen_mobile.model.data;

public class BannerItem {

    private final String title;
    private final String subtitle;
    private final String cta;
    private final String imageUrl;

    public BannerItem(String title, String subtitle, String cta) {
        this(title, subtitle, cta, null);
    }

    public BannerItem(String title, String subtitle, String cta, String imageUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.cta = cta;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getCta() {
        return cta;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
