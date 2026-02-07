package com.example.EcoGo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.CarbonRecordInterface;
import com.example.EcoGo.model.CarbonRecord;

/**
 * 碳积分记录接口控制器
 * 路径规范：/api/v1/carbon-records
 */
@RestController
@RequestMapping("/api/v1/carbon-records")
public class CarbonRecordController {
    private static final Logger logger = LoggerFactory.getLogger(CarbonRecordController.class);

    @Autowired
    private CarbonRecordInterface carbonRecordService;

    /**
     * 获取所有积分记录
     * GET /api/v1/carbon-records
     */
    @GetMapping
    public ResponseMessage<List<CarbonRecord>> getAllRecords() {
        logger.info("获取所有碳积分记录");
        List<CarbonRecord> records = carbonRecordService.getAllRecords();
        return ResponseMessage.success(records);
    }

    /**
     * 获取用户的积分记录
     * GET /api/v1/carbon-records/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseMessage<List<CarbonRecord>> getRecordsByUserId(@PathVariable String userId) {
        logger.info("获取用户碳积分记录，用户ID：{}", userId);
        List<CarbonRecord> records = carbonRecordService.getRecordsByUserId(userId);
        return ResponseMessage.success(records);
    }

    /**
     * 用户获取积分
     * POST /api/v1/carbon-records/earn
     */
    @PostMapping("/earn")
    public ResponseMessage<CarbonRecord> earnCredits(
            @RequestParam String userId,
            @RequestParam Integer credits,
            @RequestParam String source,
            @RequestParam(required = false, defaultValue = "") String description) {
        logger.info("用户 {} 获取积分：{}", userId, credits);
        CarbonRecord record = carbonRecordService.earnCredits(userId, credits, source, description);
        return ResponseMessage.success(record);
    }

    /**
     * 用户消耗积分
     * POST /api/v1/carbon-records/spend
     */
    @PostMapping("/spend")
    public ResponseMessage<CarbonRecord> spendCredits(
            @RequestParam String userId,
            @RequestParam Integer credits,
            @RequestParam String source,
            @RequestParam(required = false, defaultValue = "") String description) {
        logger.info("用户 {} 消耗积分：{}", userId, credits);
        CarbonRecord record = carbonRecordService.spendCredits(userId, credits, source, description);
        return ResponseMessage.success(record);
    }

    /**
     * 获取用户总积分
     * GET /api/v1/carbon-records/user/{userId}/total
     */
    @GetMapping("/user/{userId}/total")
    public ResponseMessage<Integer> getTotalCredits(@PathVariable String userId) {
        logger.info("获取用户总积分，用户ID：{}", userId);
        Integer total = carbonRecordService.getTotalCreditsByUserId(userId);
        return ResponseMessage.success(total);
    }
}
