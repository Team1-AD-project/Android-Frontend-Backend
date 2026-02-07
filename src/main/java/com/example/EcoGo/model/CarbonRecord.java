package com.example.EcoGo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 碳积分记录实体
 * 记录用户获取/消耗碳积分的明细
 */
@Document(collection = "transactions")
public class CarbonRecord {
    @Id
    private String id;
    private String userId;
    private String type; // EARN(获取), SPEND(消耗)
    @Field("amount")
    private Integer credits; // 积分数量
    private String source; // 来源：ACTIVITY(活动), DAILY_CHECK(每日签到), EXCHANGE(兑换), ADMIN(管理员调整)
    private String description; // 描述
    private LocalDateTime createdAt;

    public CarbonRecord() {
    }

    public CarbonRecord(String userId, String type, Integer credits, String source, String description) {
        this.userId = userId;
        this.type = type;
        this.credits = credits;
        this.source = source;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
