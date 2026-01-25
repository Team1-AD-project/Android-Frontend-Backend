package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.ActivityInterface;
import com.example.EcoGo.model.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理接口控制器
 * 路径规范：/api/v1/activities
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityInterface activityService;

    /**
     * 获取所有活动
     * GET /api/v1/activities
     */
    @GetMapping
    public ResponseMessage<List<Activity>> getAllActivities() {
        logger.info("获取所有活动列表");
        List<Activity> activities = activityService.getAllActivities();
        return ResponseMessage.success(activities);
    }

    /**
     * 根据ID获取活动
     * GET /api/v1/activities/{id}
     */
    @GetMapping("/{id}")
    public ResponseMessage<Activity> getActivityById(@PathVariable String id) {
        logger.info("获取活动详情，ID：{}", id);
        Activity activity = activityService.getActivityById(id);
        return ResponseMessage.success(activity);
    }

    /**
     * 创建活动
     * POST /api/v1/activities
     */
    @PostMapping
    public ResponseMessage<Activity> createActivity(@RequestBody Activity activity) {
        logger.info("创建新活动：{}", activity.getTitle());
        Activity created = activityService.createActivity(activity);
        return ResponseMessage.success(created);
    }

    /**
     * 更新活动
     * PUT /api/v1/activities/{id}
     */
    @PutMapping("/{id}")
    public ResponseMessage<Activity> updateActivity(
            @PathVariable String id,
            @RequestBody Activity activity) {
        logger.info("更新活动，ID：{}", id);
        Activity updated = activityService.updateActivity(id, activity);
        return ResponseMessage.success(updated);
    }

    /**
     * 删除活动
     * DELETE /api/v1/activities/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteActivity(@PathVariable String id) {
        logger.info("删除活动，ID：{}", id);
        activityService.deleteActivity(id);
        return ResponseMessage.success(null);
    }

    /**
     * 根据状态获取活动
     * GET /api/v1/activities/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseMessage<List<Activity>> getActivitiesByStatus(@PathVariable String status) {
        logger.info("按状态查询活动：{}", status);
        List<Activity> activities = activityService.getActivitiesByStatus(status);
        return ResponseMessage.success(activities);
    }

    /**
     * 发布活动
     * POST /api/v1/activities/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public ResponseMessage<Activity> publishActivity(@PathVariable String id) {
        logger.info("发布活动，ID：{}", id);
        Activity activity = activityService.publishActivity(id);
        return ResponseMessage.success(activity);
    }

    /**
     * 参加活动
     * POST /api/v1/activities/{id}/join
     */
    @PostMapping("/{id}/join")
    public ResponseMessage<Activity> joinActivity(
            @PathVariable String id,
            @RequestParam String userId) {
        logger.info("用户 {} 参加活动 {}", userId, id);
        Activity activity = activityService.joinActivity(id, userId);
        return ResponseMessage.success(activity);
    }

    /**
     * 退出活动
     * POST /api/v1/activities/{id}/leave
     */
    @PostMapping("/{id}/leave")
    public ResponseMessage<Activity> leaveActivity(
            @PathVariable String id,
            @RequestParam String userId) {
        logger.info("用户 {} 退出活动 {}", userId, id);
        Activity activity = activityService.leaveActivity(id, userId);
        return ResponseMessage.success(activity);
    }
}
