package com.example.EcoGo.dto;

import java.util.List;

public class AnalyticsSummaryDto {

    private Metric totalUsers;
    private Metric newUsers;
    private Metric activeUsers;
    private Metric totalCarbonSaved;
    private Metric averageCarbonPerUser;
    private Metric totalRevenue;
    private Metric vipRevenue;
    private Metric shopRevenue;

    private List<UserGrowthPoint> userGrowthTrend;
    private List<CarbonGrowthPoint> carbonGrowthTrend;
    private List<RevenueGrowthPoint> revenueGrowthTrend;

    private List<DistributionPoint> vipDistribution;
    private List<DistributionPoint> categoryRevenue;

    // --- NESTED CLASSES WITH FULL IMPLEMENTATION ---

    public static class Metric {
        private double currentValue;
        private double previousValue;
        private double growthRate;

        public Metric(double currentValue, double previousValue) {
            this.currentValue = currentValue;
            this.previousValue = previousValue;
            this.growthRate = (previousValue == 0) ? (currentValue > 0 ? 100.0 : 0.0) : ((currentValue - previousValue) / previousValue) * 100;
        }

        public double getCurrentValue() { return currentValue; }
        public double getPreviousValue() { return previousValue; }
        public double getGrowthRate() { return growthRate; }
    }

    public static class UserGrowthPoint {
        private String date;
        private long users;
        private long newUsers;
        private long activeUsers;

        public UserGrowthPoint(String date, long users, long newUsers, long activeUsers) {
            this.date = date;
            this.users = users;
            this.newUsers = newUsers;
            this.activeUsers = activeUsers;
        }
        public String getDate() { return date; }
        public long getUsers() { return users; }
        public long getNewUsers() { return newUsers; }
        public long getActiveUsers() { return activeUsers; }
    }

    public static class CarbonGrowthPoint {
        private String date;
        private long carbonSaved;
        private double avgPerUser;

        public CarbonGrowthPoint(String date, long carbonSaved, double avgPerUser) {
            this.date = date;
            this.carbonSaved = carbonSaved;
            this.avgPerUser = avgPerUser;
        }
        public String getDate() { return date; }
        public long getCarbonSaved() { return carbonSaved; }
        public double getAvgPerUser() { return avgPerUser; }
    }

    public static class RevenueGrowthPoint {
        private String date;
        private long vipRevenue;
        private long shopRevenue;

        public RevenueGrowthPoint(String date, long vipRevenue, long shopRevenue) {
            this.date = date;
            this.vipRevenue = vipRevenue;
            this.shopRevenue = shopRevenue;
        }
        public String getDate() { return date; }
        public long getVipRevenue() { return vipRevenue; }
        public long getShopRevenue() { return shopRevenue; }
    }

    public static class DistributionPoint {
        private String name;
        private long value;

        public DistributionPoint(String name, long value) {
            this.name = name;
            this.value = value;
        }
        public String getName() { return name; }
        public long getValue() { return value; }
    }

    // --- GETTERS AND SETTERS FOR MAIN DTO ---
    public Metric getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Metric totalUsers) { this.totalUsers = totalUsers; }
    public Metric getNewUsers() { return newUsers; }
    public void setNewUsers(Metric newUsers) { this.newUsers = newUsers; }
    public Metric getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Metric activeUsers) { this.activeUsers = activeUsers; }
    public Metric getTotalCarbonSaved() { return totalCarbonSaved; }
    public void setTotalCarbonSaved(Metric totalCarbonSaved) { this.totalCarbonSaved = totalCarbonSaved; }
    public Metric getAverageCarbonPerUser() { return averageCarbonPerUser; }
    public void setAverageCarbonPerUser(Metric averageCarbonPerUser) { this.averageCarbonPerUser = averageCarbonPerUser; }
    public Metric getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Metric totalRevenue) { this.totalRevenue = totalRevenue; }
    public Metric getVipRevenue() { return vipRevenue; }
    public void setVipRevenue(Metric vipRevenue) { this.vipRevenue = vipRevenue; }
    public Metric getShopRevenue() { return shopRevenue; }
    public void setShopRevenue(Metric shopRevenue) { this.shopRevenue = shopRevenue; }
    public List<UserGrowthPoint> getUserGrowthTrend() { return userGrowthTrend; }
    public void setUserGrowthTrend(List<UserGrowthPoint> userGrowthTrend) { this.userGrowthTrend = userGrowthTrend; }
    public List<CarbonGrowthPoint> getCarbonGrowthTrend() { return carbonGrowthTrend; }
    public void setCarbonGrowthTrend(List<CarbonGrowthPoint> carbonGrowthTrend) { this.carbonGrowthTrend = carbonGrowthTrend; }
    public List<RevenueGrowthPoint> getRevenueGrowthTrend() { return revenueGrowthTrend; }
    public void setRevenueGrowthTrend(List<RevenueGrowthPoint> revenueGrowthTrend) { this.revenueGrowthTrend = revenueGrowthTrend; }
    public List<DistributionPoint> getVipDistribution() { return vipDistribution; }
    public void setVipDistribution(List<DistributionPoint> vipDistribution) { this.vipDistribution = vipDistribution; }
    public List<DistributionPoint> getCategoryRevenue() { return categoryRevenue; }
    public void setCategoryRevenue(List<DistributionPoint> categoryRevenue) { this.categoryRevenue = categoryRevenue; }
}
