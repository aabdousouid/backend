package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Notifications;
import com.bezkoder.spring.security.postgresql.repository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    @Override
    public Notifications save(Notifications notification) {
        Notifications saved = notificationRepository.save(notification);

        // This guarantees that the saved notification (with id, timestamp, etc) is sent
        String username = saved.getRecipient().getUsername(); // Make sure this is unique and used as Principal in WebSocket
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                saved
        );

        return saved;
    }

    @Override
    public List<Notifications> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}
