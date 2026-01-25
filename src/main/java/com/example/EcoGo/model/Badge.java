package com.example.EcoGo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

/**
 * 徽章/商品 模板表
 */
@Data
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

    // ✅ 核心修正：补回分类字段，用于同类互斥！
    // 例如: "RANK", "ACHIEVEMENT", "VIP"
    private String category; 

    private BadgeIcon icon;

    @Field("is_active")
    private boolean isActive;

    @Field("created_at")
    private Date createdAt;

    @Data
    public static class BadgeIcon {
        private String url;
        @Field("color_scheme")
        private String colorScheme;
    }
}

   