package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "goods")
public class Goods {
    
    @Id
    private String id;
    
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
    private String brand;
    private String imageUrl;
    private Boolean isActive = true;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    
    // VIP兑换相关字段
    private Boolean isForRedemption = false;      // 是否可用于兑换
    private Integer redemptionPoints = 0;         // 兑换所需积分
    private Integer vipLevelRequired = 0;         // 所需VIP等级（0表示无限制）
    private Integer redemptionLimit = -1;         // 每人兑换限制（-1表示无限制）
    private Integer totalRedemptionCount = 0;     // 总兑换次数
    
    // 现金购买相关字段
    private Double cashPrice;                     // 现金购买价格（SGD）
    
    // 构造函数
    public Goods() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
    }
    
    public Goods(String name, String description, Double price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsForRedemption() { return isForRedemption; }
    public void setIsForRedemption(Boolean isForRedemption) { this.isForRedemption = isForRedemption; }
    
    public Integer getRedemptionPoints() { return redemptionPoints; }
    public void setRedemptionPoints(Integer redemptionPoints) { this.redemptionPoints = redemptionPoints; }
    
    public Integer getVipLevelRequired() { return vipLevelRequired; }
    public void setVipLevelRequired(Integer vipLevelRequired) { this.vipLevelRequired = vipLevelRequired; }
    
    public Integer getRedemptionLimit() { return redemptionLimit; }
    public void setRedemptionLimit(Integer redemptionLimit) { this.redemptionLimit = redemptionLimit; }
    
    public Integer getTotalRedemptionCount() { return totalRedemptionCount; }
    public void setTotalRedemptionCount(Integer totalRedemptionCount) { this.totalRedemptionCount = totalRedemptionCount; }
    
    public Double getCashPrice() { return cashPrice; }
    public void setCashPrice(Double cashPrice) { this.cashPrice = cashPrice; }
}