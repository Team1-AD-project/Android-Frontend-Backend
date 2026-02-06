package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    // 基础信息
    private String name;
    private String description;
    private String type; // "voucher" 或 "goods"
    private String category; // "food", "transport", "eco_product", "merchandise", "digital"
    
    // 双重价格
    private Integer pointsPrice;  // 积分价格，null表示不支持积分
    private Double cashPrice;     // 现金价格(SGD)，null表示不支持现金
    
    // 库存和可用性
    private Boolean available = true;
    private Integer stock;  // null表示无限库存
    
    // 额外属性
    private String imageUrl;
    private String brand;
    private String validUntil;  // 优惠券有效期
    private List<String> tags = new ArrayList<>();
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    
    // 构造函数
    public Product() {
        this.available = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.tags = new ArrayList<>();
    }
    
    public Product(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.available = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.tags = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Integer getPointsPrice() { return pointsPrice; }
    public void setPointsPrice(Integer pointsPrice) { this.pointsPrice = pointsPrice; }
    
    public Double getCashPrice() { return cashPrice; }
    public void setCashPrice(Double cashPrice) { this.cashPrice = cashPrice; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getValidUntil() { return validUntil; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
