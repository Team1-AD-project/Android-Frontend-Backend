package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日目标实体
 * 用户每日步数、出行、CO2节省目标管理
 */
@Document(collection = "daily_goals")
public class DailyGoal {
    @Id
    private String id;
    private String userId; // 用户ID
    private LocalDate date; // 目标日期
    private Integer stepGoal; // 步数目标
    private Integer currentSteps; // 当前步数
    private Integer tripGoal; // 出行次数目标
    private Integer currentTrips; // 当前出行次数
    private Float co2SavedGoal; // CO2节省目标(kg)
    private Float currentCo2Saved; // 当前CO2节省量(kg)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailyGoal() {
        this.stepGoal = 10000;
        this.tripGoal = 3;
        this.co2SavedGoal = 2.0f;
        this.currentSteps = 0;
        this.currentTrips = 0;
        this.currentCo2Saved = 0f;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DailyGoal(String userId, LocalDate date) {
        this();
        this.userId = userId;
        this.date = date;
    }

    /**
     * 计算总体完成百分比
     */
    public Integer getOverallProgress() {
        int stepProgress = (int) ((currentSteps.floatValue() / stepGoal) * 100);
        int tripProgress = (int) ((currentTrips.floatValue() / tripGoal) * 100);
        int co2Progress = (int) ((currentCo2Saved / co2SavedGoal) * 100);
        return (stepProgress + tripProgress + co2Progress) / 3;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getStepGoal() {
        return stepGoal;
    }

    public void setStepGoal(Integer stepGoal) {
        this.stepGoal = stepGoal;
    }

    public Integer getCurrentSteps() {
        return currentSteps;
    }

    public void setCurrentSteps(Integer currentSteps) {
        this.currentSteps = currentSteps;
    }

    public Integer getTripGoal() {
        return tripGoal;
    }

    public void setTripGoal(Integer tripGoal) {
        this.tripGoal = tripGoal;
    }

    public Integer getCurrentTrips() {
        return currentTrips;
    }

    public void setCurrentTrips(Integer currentTrips) {
        this.currentTrips = currentTrips;
    }

    public Float getCo2SavedGoal() {
        return co2SavedGoal;
    }

    public void setCo2SavedGoal(Float co2SavedGoal) {
        this.co2SavedGoal = co2SavedGoal;
    }

    public Float getCurrentCo2Saved() {
        return currentCo2Saved;
    }

    public void setCurrentCo2Saved(Float currentCo2Saved) {
        this.currentCo2Saved = currentCo2Saved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
