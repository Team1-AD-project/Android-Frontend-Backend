package com.example.EcoGo.dto;

import java.util.List;

/**
 * 商品DTO - 返回给前端的统一格式
 */
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private String type;  // "voucher" 或 "goods"
    private String category;
    private Integer pointsPrice;
    private Double cashPrice;
    private Boolean available;
    private Integer stock;
    private String imageUrl;
    private String brand;
    private String validUntil;
    private List<String> tags;
    
    // 构造函数
    public ProductDTO() {}
    
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
}
