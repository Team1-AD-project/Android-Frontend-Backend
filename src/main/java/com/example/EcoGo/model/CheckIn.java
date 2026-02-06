package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录实体
 * 用户每日签到管理
 */
@Document(collection = "check_ins")
public class CheckIn {
    @Id
    private String id;
    private String userId; // 用户ID
    private LocalDate checkInDate; // 签到日期
    private Integer pointsEarned; // 当次签到获得的积分
    private Integer consecutiveDays; // 连续签到天数
    private LocalDateTime createdAt;

    public CheckIn() {
        this.createdAt = LocalDateTime.now();
        this.pointsEarned = 10; // 默认签到积分
    }

    public CheckIn(String userId, LocalDate checkInDate, Integer consecutiveDays) {
        this.userId = userId;
        this.checkInDate = checkInDate;
        this.consecutiveDays = consecutiveDays;
        this.pointsEarned = calculatePoints(consecutiveDays);
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 根据连续签到天数计算积分
     */
    private Integer calculatePoints(Integer consecutiveDays) {
        if (consecutiveDays >= 7) {
            return 20; // 连续7天奖励
        } else if (consecutiveDays >= 3) {
            return 15; // 连续3天奖励
        }
        return 10; // 基础积分
    }

    // Getters and Setters
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

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
