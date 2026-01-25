package com.example.EcoGo.dto;

import java.util.List;
import java.util.Map;

/**
 * 碳排热力图数据 DTO
 */
public class HeatmapDataDTO {
    private String region;
    private Double latitude;
    private Double longitude;
    private Long emissionValue;
    private Long reductionValue;
    private String intensity; // LOW, MEDIUM, HIGH

    // 静态内部类用于汇总数据
    public static class HeatmapSummary {
        private List<HeatmapDataDTO> dataPoints;
        private Map<String, Long> regionStats;
        private Long totalEmissions;
        private Long totalReductions;

        public List<HeatmapDataDTO> getDataPoints() {
            return dataPoints;
        }

        public void setDataPoints(List<HeatmapDataDTO> dataPoints) {
            this.dataPoints = dataPoints;
        }

        public Map<String, Long> getRegionStats() {
            return regionStats;
        }

        public void setRegionStats(Map<String, Long> regionStats) {
            this.regionStats = regionStats;
        }

        public Long getTotalEmissions() {
            return totalEmissions;
        }

        public void setTotalEmissions(Long totalEmissions) {
            this.totalEmissions = totalEmissions;
        }

        public Long getTotalReductions() {
            return totalReductions;
        }

        public void setTotalReductions(Long totalReductions) {
            this.totalReductions = totalReductions;
        }
    }

    // Getters and Setters
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getEmissionValue() {
        return emissionValue;
    }

    public void setEmissionValue(Long emissionValue) {
        this.emissionValue = emissionValue;
    }

    public Long getReductionValue() {
        return reductionValue;
    }

    public void setReductionValue(Long reductionValue) {
        this.reductionValue = reductionValue;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }
}
