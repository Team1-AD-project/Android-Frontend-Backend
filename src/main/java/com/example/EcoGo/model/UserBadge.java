package com.example.EcoGo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 用户持有的徽章 (背包)
 */
@Data
@Document(collection = "user_badges")
public class UserBadge {

    @Id
    private String id;

    @Field("user_id")
    private String userId; // 谁买的

    @Field("badge_id")
    private String badgeId; // 买了哪个

    @Field("unlocked_at")
    private Date unlockedAt; // 购买时间

    @Field("is_display")
    private boolean isDisplay; // ✅ 是否佩戴 (点击显示)

    @Field("created_at")
    private Date createdAt;
}