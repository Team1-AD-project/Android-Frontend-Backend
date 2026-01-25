package com.example.EcoGo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 徽章模板表
 * 对应文档：mongodbv2(1).md - badges
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "badges") // 对应 MongoDB 的 badges 集合
public class Badge {

    @Id
    private String id; // 数据库主键

    @Field("badge_id")
    private String badgeId; // 业务ID，如 "badge_001"

    // 多语言名称，如 {"zh": "碳减排达人", "en": "Carbon Saver"}
    private Map<String, String> name; 
    
    // 多语言描述
    private Map<String, String> description;

    // 图标信息
    private BadgeIcon icon; 

    private String tier;     // gold, silver 等
    private String category; // 分类
    private String rarity;   // 稀有度

    // 🔥 核心：解锁条件 (对象)
    @Field("unlock_criteria")
    private UnlockCriteria unlockCriteria; 

    // 🔥 核心：奖励内容 (对象)
    private BadgeRewards rewards; 

    @Field("is_active")
    private boolean isActive; // 是否激活
    
    @Field("is_hidden")
    private boolean isHidden; 

    @Field("created_at")
    private Date createdAt;
    
    @Field("updated_at")
    private Date updatedAt;

    // --- 内部类 (对应嵌套 JSON) ---

    @Data
    public static class BadgeIcon {
        private String url;
        @Field("color_scheme")
        private String colorScheme;
    }

    @Data
    public static class UnlockCriteria {
        private String type;      // 如 "CARBON_SAVED"
        private String metric;    // 核心指标，如 "total_carbon"
        private double threshold; // 阈值，如 1000
        private String unit;      // 单位，如 "g"
    }

    @Data
    public static class BadgeRewards {
        private int points;         // 奖励积分
        private String title;       // 奖励称号
        private List<String> perks; // 特权
    }
}