package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "inventory")
public class Inventory {

    @Id
    private String id;       
    private String goodsId;
    private Integer quantity;
    private Date updatedAt;

    public Inventory() {}

    public Inventory(String goodsId, Integer quantity) {
        this.id = goodsId;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.updatedAt = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGoodsId() { return goodsId; }
    public void setGoodsId(String goodsId) { this.goodsId = goodsId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
