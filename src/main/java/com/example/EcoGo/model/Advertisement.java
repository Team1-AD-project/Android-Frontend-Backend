package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;

@Document(collection = "advertisements")
public class Advertisement {
    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("status")
    private String status; // Active, Inactive, Paused

    @Field("start_date") // Explicitly map to the database field name
    private LocalDate startDate;

    @Field("end_date") // Explicitly map to the database field name
    private LocalDate endDate;

    public Advertisement() {
    }

    public Advertisement(String name, String status, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters remain the same...

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
