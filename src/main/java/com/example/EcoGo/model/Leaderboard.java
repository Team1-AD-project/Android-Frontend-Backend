package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "leaderboard")
public class Leaderboard {

    @Id
    private String id;

    @Field("period")
    private String period;

    @Field("startDate")
    private LocalDateTime startDate;

    @Field("endDate")
    private LocalDateTime endDate;

    @Field("type")
    private String type;

    @Field("status")
    private String status;

    @Field("rankings")
    private List<Ranking> rankings;

    @Field("createdAt")
    private LocalDateTime createdAt;

    // Inner class representing a single entry in the rankings array
    public static class Ranking {
        private int rank;
        @Field("userId")
        private String userId;
        private String nickname;
        private int steps;
        @Field("isVip")
        private boolean isVip;
        private String avatar;

        // Getters and setters for Ranking
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public int getSteps() { return steps; }
        public void setSteps(int steps) { this.steps = steps; }
        public boolean isVip() { return isVip; }
        public void setVip(boolean vip) { isVip = vip; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    // Getters and setters for Leaderboard

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Ranking> getRankings() {
        return rankings;
    }

    public void setRankings(List<Ranking> rankings) {
        this.rankings = rankings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
