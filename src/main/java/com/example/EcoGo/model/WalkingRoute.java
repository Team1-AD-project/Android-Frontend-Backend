package com.example.EcoGo.model;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "walking_routes")
public class WalkingRoute {
    @Id
    private String id;
    private String title;
    private String time;
    private String distance;
    private String calories;
    private List<String> tags;
    private String description;

    public WalkingRoute() {}

    public WalkingRoute(String id, String title, String time, String distance, String calories, List<String> tags, String description) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.distance = distance;
        this.calories = calories;
        this.tags = tags;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
