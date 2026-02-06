package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.Notification;
import java.util.List;

public interface NotificationInterface {
    Notification createNotification(String userId, String type, String title, String message, String actionUrl);
    List<Notification> getUserNotifications(String userId);
    List<Notification> getUnreadNotifications(String userId);
    Notification markAsRead(String notificationId);
    void markAllAsRead(String userId);
}
