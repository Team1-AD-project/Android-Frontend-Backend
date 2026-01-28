package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 订单项实体
 * 关联 Order 和 Goods，实现订单与商品的多对多关系
 */
@Document(collection = "order_items")
public class OrderItem {

    @Id
    private String id;

    @Field("order_id")
    private String orderId;         // 关联订单ID

    @Field("goods_id")
    private String goodsId;         // 关联商品ID

    @Field("goods_name")
    private String goodsName;       // 商品名称（冗余存储）

    private Integer quantity;       // 购买数量

    private Double price;           // 单价

    private Double subtotal;        // 小计金额

    @Field("created_at")
    private Date createdAt;

    // Constructors
    public OrderItem() {
        this.createdAt = new Date();
    }

    public OrderItem(String orderId, String goodsId, String goodsName, Integer quantity, Double price) {
        this.orderId = orderId;
        this.goodsId = goodsId;
        this.goodsName = goodsName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = price * quantity;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getGoodsId() { return goodsId; }
    public void setGoodsId(String goodsId) { this.goodsId = goodsId; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
