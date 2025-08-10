package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Notifications;

import java.util.List;

public interface NotificationService {

    Notifications save(Notifications notifications);

    List<Notifications> getUserNotifications(Long userId);

    void markAsRead (Long notificationId);

    void markAllAsRead();
}
