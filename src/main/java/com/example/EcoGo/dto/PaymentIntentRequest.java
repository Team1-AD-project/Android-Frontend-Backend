package com.example.EcoGo.dto;

/**
 * 创建支付Intent请求
 */
public class PaymentIntentRequest {
    private String userId;
    private String productId;
    
    // 构造函数
    public PaymentIntentRequest() {}
    
    public PaymentIntentRequest(String userId, String productId) {
        this.userId = userId;
        this.productId = productId;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
