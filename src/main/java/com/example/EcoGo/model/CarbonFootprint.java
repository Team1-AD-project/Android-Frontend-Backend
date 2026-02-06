package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 碳足迹实体
 * 用户碳足迹数据统计
 */
@Document(collection = "carbon_footprints")
public class CarbonFootprint {
    @Id
    private String id;
    private String userId; // 用户ID
    private String period; // 统计周期：daily, weekly, monthly
    private LocalDate startDate; // 统计开始日期
    private LocalDate endDate; // 统计结束日期
    private Float co2Saved; // CO2节省量(kg)
    private Integer equivalentTrees; // 相当于多少棵树
    private Integer tripsByBus; // 乘坐巴士次数
    private Integer tripsByWalking; // 步行次数
    private Integer tripsByBicycle; // 骑行次数
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CarbonFootprint() {
        this.co2Saved = 0f;
        this.equivalentTrees = 0;
        this.tripsByBus = 0;
        this.tripsByWalking = 0;
        this.tripsByBicycle = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CarbonFootprint(String userId, String period, LocalDate startDate, LocalDate endDate) {
        this();
        this.userId = userId;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 根据CO2节省量计算等效树木数量
     * 平均一棵树每年吸收约5kg CO2
     */
    public void calculateEquivalentTrees() {
        this.equivalentTrees = (int) Math.ceil(co2Saved / 5.0);
    }

    /**
     * 添加一次出行记录
     */
    public void addTrip(String tripType, Float co2Amount) {
        this.co2Saved += co2Amount;
        switch (tripType.toLowerCase()) {
            case "bus":
                this.tripsByBus++;
                break;
            case "walking":
                this.tripsByWalking++;
                break;
            case "bicycle":
                this.tripsByBicycle++;
                break;
        }
        calculateEquivalentTrees();
        this.updatedAt = LocalDateTime.now();
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Float getCo2Saved() {
        return co2Saved;
    }

    public void setCo2Saved(Float co2Saved) {
        this.co2Saved = co2Saved;
        calculateEquivalentTrees();
    }

    public Integer getEquivalentTrees() {
        return equivalentTrees;
    }

    public void setEquivalentTrees(Integer equivalentTrees) {
        this.equivalentTrees = equivalentTrees;
    }

    public Integer getTripsByBus() {
        return tripsByBus;
    }

    public void setTripsByBus(Integer tripsByBus) {
        this.tripsByBus = tripsByBus;
    }

    public Integer getTripsByWalking() {
        return tripsByWalking;
    }

    public void setTripsByWalking(Integer tripsByWalking) {
        this.tripsByWalking = tripsByWalking;
    }

    public Integer getTripsByBicycle() {
        return tripsByBicycle;
    }

    public void setTripsByBicycle(Integer tripsByBicycle) {
        this.tripsByBicycle = tripsByBicycle;
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
