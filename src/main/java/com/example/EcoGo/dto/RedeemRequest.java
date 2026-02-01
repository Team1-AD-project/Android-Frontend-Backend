package com.example.EcoGo.dto;

/**
 * 积分兑换请求
 */
public class RedeemRequest {
    private String userId;
    private String productId;
    private String productType; // "voucher" or "goods"
    private Integer quantity = 1;
    
    // 构造函数
    public RedeemRequest() {}
    
    public RedeemRequest(String userId, String productId, String productType) {
        this.userId = userId;
        this.productId = productId;
        this.productType = productType;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
