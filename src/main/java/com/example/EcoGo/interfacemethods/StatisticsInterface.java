package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.dto.DashboardStatsDTO;
import com.example.EcoGo.dto.HeatmapDataDTO;

/**
 * 统计分析服务接口
 * 提供运营仪表盘所需的各类统计数据
 */
public interface StatisticsInterface {

    /**
     * 获取仪表盘综合统计数据
     */
    DashboardStatsDTO getDashboardStats();

    /**
     * 获取总碳减排量
     */
    Long getTotalCarbonReduction();

    /**
     * 获取活跃用户数
     * @param days 最近多少天内活跃
     */
    Long getActiveUserCount(int days);

    /**
     * 获取奖励兑换量
     */
    Long getRedemptionVolume();

    /**
     * 获取碳排热力图数据
     */
    HeatmapDataDTO.HeatmapSummary getEmissionHeatmap();
}
