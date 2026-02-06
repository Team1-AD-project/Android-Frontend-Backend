package com.example.EcoGo.dto;

/**
 * 确认支付请求
 */
public class ConfirmPaymentRequest {
    private String userId;
    private String productId;
    private String paymentIntentId;
    
    // 构造函数
    public ConfirmPaymentRequest() {}
    
    public ConfirmPaymentRequest(String userId, String productId, String paymentIntentId) {
        this.userId = userId;
        this.productId = productId;
        this.paymentIntentId = paymentIntentId;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
}
