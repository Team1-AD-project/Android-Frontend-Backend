package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "faculties")
public class Faculty {
    @Id
    private String id;
    private String name;
    private String color;
    private String icon;
    private Integer score;
    private Integer rank;

    public Faculty() {}

    public Faculty(String id, String name, String color, String icon, Integer score, Integer rank) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.score = score;
        this.rank = rank;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
