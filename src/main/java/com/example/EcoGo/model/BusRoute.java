package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bus_routes")
public class BusRoute {
    @Id
    private String id;
    private String name;
    private String from;
    private String to;
    private String color;
    private String status;
    private String time;
    private String crowd;

    public BusRoute() {}

    public BusRoute(String id, String name, String from, String to, String color, String status, String time, String crowd) {
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
        this.color = color;
        this.status = status;
        this.time = time;
        this.crowd = crowd;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCrowd() {
        return crowd;
    }

    public void setCrowd(String crowd) {
        this.crowd = crowd;
    }
}
