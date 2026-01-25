package com.example.EcoGo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 用户徽章关联表
 * 对应文档：mongodbv2(1).md - user_badges
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_badges") // 对应 MongoDB 的 user_badges 集合
public class UserBadge {

    @Id
    private String id;

    @Field("user_id")
    private String userId; // 谁获得的

    @Field("badge_id")
    private String badgeId; // 获得了哪个 (对应 Badge 类的 badgeId)

    @Field("unlocked_at")
    private Date unlockedAt; // 什么时候获得的

    @Field("is_display")
    private boolean isDisplay; // 是否佩戴在主页

    @Field("created_at")
    private Date createdAt;
}