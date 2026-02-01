package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.NotificationInterface;
import com.example.EcoGo.model.Notification;
import com.example.EcoGo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationImplementation implements NotificationInterface {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(String userId, String type, String title, String message, String actionUrl) {
        Notification notification = new Notification(userId, type, title, message);
        notification.setActionUrl(actionUrl);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    @Override
    public Notification markAsRead(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            return notificationRepository.save(notification);
        }
        throw new RuntimeException("Notification not found");
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}
