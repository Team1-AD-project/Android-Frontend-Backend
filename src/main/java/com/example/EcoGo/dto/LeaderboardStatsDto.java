package com.example.EcoGo.dto;

import com.example.EcoGo.model.Ranking;
import org.springframework.data.domain.Page;

public class LeaderboardStatsDto {

    private Page<Ranking> rankingsPage;
    private long totalCarbonSaved;
    private long totalVipUsers;

    public LeaderboardStatsDto(Page<Ranking> rankingsPage, long totalCarbonSaved, long totalVipUsers) {
        this.rankingsPage = rankingsPage;
        this.totalCarbonSaved = totalCarbonSaved;
        this.totalVipUsers = totalVipUsers;
    }

    // Getters and Setters
    public Page<Ranking> getRankingsPage() {
        return rankingsPage;
    }

    public void setRankingsPage(Page<Ranking> rankingsPage) {
        this.rankingsPage = rankingsPage;
    }

    public long getTotalCarbonSaved() {
        return totalCarbonSaved;
    }

    public void setTotalCarbonSaved(long totalCarbonSaved) {
        this.totalCarbonSaved = totalCarbonSaved;
    }

    public long getTotalVipUsers() {
        return totalVipUsers;
    }

    public void setTotalVipUsers(long totalVipUsers) {
        this.totalVipUsers = totalVipUsers;
    }
}
