package com.example.EcoGo.service;

import com.example.EcoGo.dto.DashboardStatsDTO;
import com.example.EcoGo.dto.HeatmapDataDTO;
import com.example.EcoGo.interfacemethods.StatisticsInterface;
import com.example.EcoGo.model.CarbonRecord;
import com.example.EcoGo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsImplementation implements StatisticsInterface {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private CarbonRecordRepository carbonRecordRepository;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 用户统计 (Temporarily disabled)
        stats.setTotalUsers(0L);
        stats.setActiveUsers(0L);

        // 广告统计
        long totalAds = advertisementRepository.count();
        long activeAds = advertisementRepository.findByStatus("Active").size();
        stats.setTotalAdvertisements(totalAds);
        stats.setActiveAdvertisements(activeAds);

        // 活动统计
        long totalActivities = activityRepository.count();
        long ongoingActivities = activityRepository.findByStatus("ONGOING").size()
                + activityRepository.findByStatus("PUBLISHED").size();
        stats.setTotalActivities(totalActivities);
        stats.setOngoingActivities(ongoingActivities);

        // 碳积分统计 (Temporarily disabled)
        stats.setTotalCarbonCredits(0L);

        // 碳减排量统计
        Long totalReduction = getTotalCarbonReduction();
        stats.setTotalCarbonReduction(totalReduction);

        // 兑换量统计
        Long redemptionVol = getRedemptionVolume();
        stats.setRedemptionVolume(redemptionVol);

        return stats;
    }

    @Override
    public Long getTotalCarbonReduction() {
        List<CarbonRecord> earnRecords = carbonRecordRepository.findByType("EARN");
        return earnRecords.stream()
                .mapToLong(r -> r.getCredits() != null ? r.getCredits() : 0)
                .sum();
    }

    @Override
    public Long getActiveUserCount(int days) {
        // This implementation now returns 0 as it depends on user data
        return 0L;
    }

    @Override
    public Long getRedemptionVolume() {
        List<CarbonRecord> redeemRecords = carbonRecordRepository.findBySourceAndType("EXCHANGE", "SPEND");
        return redeemRecords.stream()
                .mapToLong(r -> r.getCredits() != null ? r.getCredits() : 0)
                .sum();
    }

    @Override
    public HeatmapDataDTO.HeatmapSummary getEmissionHeatmap() {
        HeatmapDataDTO.HeatmapSummary summary = new HeatmapDataDTO.HeatmapSummary();
        List<HeatmapDataDTO> dataPoints = generateMockHeatmapData();
        summary.setDataPoints(dataPoints);
        // ... (rest of the method is unchanged)
        return summary;
    }

    // This method is unchanged
    private List<HeatmapDataDTO> generateMockHeatmapData() {
        List<HeatmapDataDTO> dataPoints = new ArrayList<>();
        // ... (rest of the method is unchanged)
        return dataPoints;
    }
}
