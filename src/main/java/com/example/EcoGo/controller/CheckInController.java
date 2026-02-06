package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.CheckInInterface;
import com.example.EcoGo.model.CheckIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签到管理接口控制器
 * 路径规范：/api/v1/checkin
 */
@RestController
@RequestMapping("/api/v1/checkin")
public class CheckInController {
    private static final Logger logger = LoggerFactory.getLogger(CheckInController.class);

    @Autowired
    private CheckInInterface checkInService;

    /**
     * 执行签到
     * POST /api/v1/checkin
     */
    @PostMapping
    public ResponseMessage<Map<String, Object>> performCheckIn(@RequestParam String userId) {
        logger.info("用户签到，userID：{}", userId);
        CheckIn checkIn = checkInService.performCheckIn(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("pointsEarned", checkIn.getPointsEarned());
        response.put("consecutiveDays", checkIn.getConsecutiveDays());
        response.put("message", "签到成功！");
        
        return ResponseMessage.success(response);
    }

    /**
     * 获取签到状态
     * GET /api/v1/checkin/status/{userId}
     */
    @GetMapping("/status/{userId}")
    public ResponseMessage<Map<String, Object>> getCheckInStatus(@PathVariable String userId) {
        logger.info("获取签到状态，userID：{}", userId);
        
        LocalDate today = LocalDate.now();
        CheckIn todayCheckIn = checkInService.getCheckInStatus(userId, today);
        Integer consecutiveDays = checkInService.getConsecutiveDays(userId);
        long totalCheckIns = checkInService.getUserCheckInHistory(userId).size();
        
        Map<String, Object> status = new HashMap<>();
        status.put("userId", userId);
        status.put("lastCheckInDate", todayCheckIn != null ? todayCheckIn.getCheckInDate().toString() : null);
        status.put("consecutiveDays", consecutiveDays);
        status.put("totalCheckIns", totalCheckIns);
        status.put("pointsEarned", todayCheckIn != null ? todayCheckIn.getPointsEarned() : 0);
        
        return ResponseMessage.success(status);
    }

    /**
     * 获取签到历史
     * GET /api/v1/checkin/history/{userId}
     */
    @GetMapping("/history/{userId}")
    public ResponseMessage<List<CheckIn>> getCheckInHistory(@PathVariable String userId) {
        logger.info("获取签到历史，userID：{}", userId);
        List<CheckIn> history = checkInService.getUserCheckInHistory(userId);
        return ResponseMessage.success(history);
    }
}
