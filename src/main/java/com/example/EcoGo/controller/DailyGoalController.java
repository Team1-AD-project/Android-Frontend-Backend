package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.DailyGoalInterface;
import com.example.EcoGo.model.DailyGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 每日目标管理接口控制器
 * 路径规范：/api/v1/goals
 */
@RestController
@RequestMapping("/api/v1/goals")
public class DailyGoalController {
    private static final Logger logger = LoggerFactory.getLogger(DailyGoalController.class);

    @Autowired
    private DailyGoalInterface dailyGoalService;

    /**
     * 获取今日目标
     * GET /api/v1/goals/daily/{userId}
     */
    @GetMapping("/daily/{userId}")
    public ResponseMessage<DailyGoal> getDailyGoal(@PathVariable String userId) {
        logger.info("获取今日目标，userID：{}", userId);
        DailyGoal goal = dailyGoalService.getDailyGoal(userId, LocalDate.now());
        return ResponseMessage.success(goal);
    }

    /**
     * 更新今日目标进度
     * PUT /api/v1/goals/daily/{userId}
     */
    @PutMapping("/daily/{userId}")
    public ResponseMessage<DailyGoal> updateDailyGoal(
            @PathVariable String userId,
            @RequestBody Map<String, Object> updates) {
        logger.info("更新今日目标，userID：{}", userId);
        
        Integer steps = updates.containsKey("currentSteps") ? 
                ((Number) updates.get("currentSteps")).intValue() : null;
        Integer trips = updates.containsKey("currentTrips") ? 
                ((Number) updates.get("currentTrips")).intValue() : null;
        Float co2Saved = updates.containsKey("currentCo2Saved") ? 
                ((Number) updates.get("currentCo2Saved")).floatValue() : null;
        
        DailyGoal goal = dailyGoalService.updateDailyGoal(userId, steps, trips, co2Saved);
        return ResponseMessage.success(goal);
    }
}
