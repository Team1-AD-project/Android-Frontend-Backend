package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vouchers")
public class Voucher {
    @Id
    private String id;
    private String name;
    private Integer cost;
    private String color;
    private String icon;
    private String description;

    public Voucher() {}

    public Voucher(String id, String name, Integer cost, String color, String icon, String description) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.color = color;
        this.icon = icon;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
