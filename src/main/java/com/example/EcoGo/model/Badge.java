package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

/**
 * 徽章/商品 模板表
 */
@Document(collection = "badges")
public class Badge {

    @Id
    private String id;

    @Field("badge_id")
    private String badgeId;

    private Map<String, String> name;
    private Map<String, String> description;

    @Field("purchase_cost")
    private int purchaseCost;

    private String category;

    private BadgeIcon icon;

    @Field("is_active")
    private boolean isActive;

    @Field("created_at")
    private Date createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public int getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(int purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BadgeIcon getIcon() {
        return icon;
    }

    public void setIcon(BadgeIcon icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public static class BadgeIcon {
        private String url;
        @Field("color_scheme")
        private String colorScheme;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getColorScheme() {
            return colorScheme;
        }

        public void setColorScheme(String colorScheme) {
            this.colorScheme = colorScheme;
        }
    }
}
