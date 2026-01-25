package com.example.EcoGo.controller;

import com.example.EcoGo.dto.DashboardStatsDTO;
import com.example.EcoGo.dto.HeatmapDataDTO;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.StatisticsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 统计分析接口控制器
 * 提供运营仪表盘所需的各类统计数据 API
 * 路径规范：/api/v1/statistics
 */
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsInterface statisticsService;

    /**
     * 获取仪表盘综合统计数据
     * GET /api/v1/statistics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseMessage<DashboardStatsDTO> getDashboardStats() {
        logger.info("获取仪表盘统计数据");
        DashboardStatsDTO stats = statisticsService.getDashboardStats();
        return ResponseMessage.success(stats);
    }

    /**
     * 获取总碳减排量
     * GET /api/v1/statistics/carbon-reduction
     */
    @GetMapping("/carbon-reduction")
    public ResponseMessage<Long> getTotalCarbonReduction() {
        logger.info("获取总碳减排量");
        Long totalReduction = statisticsService.getTotalCarbonReduction();
        return ResponseMessage.success(totalReduction);
    }

    /**
     * 获取活跃用户数
     * GET /api/v1/statistics/active-users?days=30
     */
    @GetMapping("/active-users")
    public ResponseMessage<Long> getActiveUserCount(
            @RequestParam(defaultValue = "30") int days) {
        logger.info("获取活跃用户数，时间范围：{}天", days);
        Long activeUsers = statisticsService.getActiveUserCount(days);
        return ResponseMessage.success(activeUsers);
    }

    /**
     * 获取奖励兑换量
     * GET /api/v1/statistics/redemption-volume
     */
    @GetMapping("/redemption-volume")
    public ResponseMessage<Long> getRedemptionVolume() {
        logger.info("获取奖励兑换量");
        Long volume = statisticsService.getRedemptionVolume();
        return ResponseMessage.success(volume);
    }

    /**
     * 获取碳排热力图数据
     * GET /api/v1/statistics/emission-heatmap
     */
    @GetMapping("/emission-heatmap")
    public ResponseMessage<HeatmapDataDTO.HeatmapSummary> getEmissionHeatmap() {
        logger.info("获取碳排热力图数据");
        HeatmapDataDTO.HeatmapSummary heatmap = statisticsService.getEmissionHeatmap();
        return ResponseMessage.success(heatmap);
    }
}
