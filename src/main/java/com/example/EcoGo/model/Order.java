package com.example.EcoGo.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")  // 确保这个注解正确，表示这是 MongoDB 的一个集合
public class Order {

    private String id;         // 订单 ID
    private String userId;     // 用户 ID
    private String productId;  // 商品 ID
    private int quantity;      // 商品数量
    private double totalAmount; // 总金额
    private String status;     // 订单状态（如：未付款、已付款、已发货）

    // Getter 和 Setter 方法

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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}