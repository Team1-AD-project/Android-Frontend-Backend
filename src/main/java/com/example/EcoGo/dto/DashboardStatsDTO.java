package com.example.EcoGo.dto;

/**
 * 仪表盘统计数据 DTO
 */
public class DashboardStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalAdvertisements;
    private Long activeAdvertisements;
    private Long totalActivities;
    private Long ongoingActivities;
    private Long totalCarbonCredits;
    private Long totalCarbonReduction;
    private Long redemptionVolume;

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getTotalAdvertisements() {
        return totalAdvertisements;
    }

    public void setTotalAdvertisements(Long totalAdvertisements) {
        this.totalAdvertisements = totalAdvertisements;
    }

    public Long getActiveAdvertisements() {
        return activeAdvertisements;
    }

    public void setActiveAdvertisements(Long activeAdvertisements) {
        this.activeAdvertisements = activeAdvertisements;
    }

    public Long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Long getOngoingActivities() {
        return ongoingActivities;
    }

    public void setOngoingActivities(Long ongoingActivities) {
        this.ongoingActivities = ongoingActivities;
    }

    public Long getTotalCarbonCredits() {
        return totalCarbonCredits;
    }

    public void setTotalCarbonCredits(Long totalCarbonCredits) {
        this.totalCarbonCredits = totalCarbonCredits;
    }

    public Long getTotalCarbonReduction() {
        return totalCarbonReduction;
    }

    public void setTotalCarbonReduction(Long totalCarbonReduction) {
        this.totalCarbonReduction = totalCarbonReduction;
    }

    public Long getRedemptionVolume() {
        return redemptionVolume;
    }

    public void setRedemptionVolume(Long redemptionVolume) {
        this.redemptionVolume = redemptionVolume;
    }
}
