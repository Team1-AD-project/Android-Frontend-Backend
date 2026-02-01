package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.CarbonFootprintInterface;
import com.example.EcoGo.model.CarbonFootprint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 碳足迹管理接口控制器
 * 路径规范：/api/v1/carbon
 */
@RestController
@RequestMapping("/api/v1/carbon")
public class CarbonFootprintController {
    private static final Logger logger = LoggerFactory.getLogger(CarbonFootprintController.class);

    @Autowired
    private CarbonFootprintInterface carbonFootprintService;

    /**
     * 获取碳足迹数据
     * GET /api/v1/carbon/{userId}?period=monthly
     */
    @GetMapping("/{userId}")
    public ResponseMessage<CarbonFootprint> getCarbonFootprint(
            @PathVariable String userId,
            @RequestParam(defaultValue = "monthly") String period) {
        logger.info("获取碳足迹数据，userID：{}，period：{}", userId, period);
        CarbonFootprint footprint = carbonFootprintService.getCarbonFootprint(userId, period);
        return ResponseMessage.success(footprint);
    }

    /**
     * 记录一次出行
     * POST /api/v1/carbon/record
     */
    @PostMapping("/record")
    public ResponseMessage<CarbonFootprint> recordTrip(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String tripType = (String) request.get("tripType");
        Float distance = ((Number) request.get("distance")).floatValue();
        
        logger.info("记录出行，userID：{}，type：{}，distance：{}", userId, tripType, distance);
        CarbonFootprint footprint = carbonFootprintService.recordTrip(userId, tripType, distance);
        return ResponseMessage.success(footprint);
    }
}
