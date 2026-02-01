package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.NotificationInterface;
import com.example.EcoGo.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知管理接口控制器
 * 路径规范：/api/v1/notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationInterface notificationService;

    /**
     * 获取用户通知列表
     * GET /api/v1/notifications/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseMessage<List<Notification>> getUserNotifications(@PathVariable String userId) {
        logger.info("获取通知列表，userID：{}", userId);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseMessage.success(notifications);
    }

    /**
     * 获取未读通知
     * GET /api/v1/notifications/{userId}/unread
     */
    @GetMapping("/{userId}/unread")
    public ResponseMessage<List<Notification>> getUnreadNotifications(@PathVariable String userId) {
        logger.info("获取未读通知，userID：{}", userId);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseMessage.success(notifications);
    }

    /**
     * 创建通知
     * POST /api/v1/notifications
     */
    @PostMapping
    public ResponseMessage<Notification> createNotification(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String type = request.get("type");
        String title = request.get("title");
        String message = request.get("message");
        String actionUrl = request.get("actionUrl");
        
        logger.info("创建通知，userID：{}，type：{}", userId, type);
        Notification notification = notificationService.createNotification(userId, type, title, message, actionUrl);
        return ResponseMessage.success(notification);
    }

    /**
     * 标记通知为已读
     * POST /api/v1/notifications/{notificationId}/read
     */
    @PostMapping("/{notificationId}/read")
    public ResponseMessage<Notification> markAsRead(@PathVariable String notificationId) {
        logger.info("标记通知已读，notificationID：{}", notificationId);
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseMessage.success(notification);
    }

    /**
     * 标记所有通知为已读
     * POST /api/v1/notifications/{userId}/read-all
     */
    @PostMapping("/{userId}/read-all")
    public ResponseMessage<Void> markAllAsRead(@PathVariable String userId) {
        logger.info("标记所有通知已读，userID：{}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseMessage.success(null);
    }
}
