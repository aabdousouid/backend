package com.bezkoder.spring.security.postgresql.services;

import com.bezkoder.spring.security.postgresql.models.Notifications;
import com.bezkoder.spring.security.postgresql.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    @Override
    public Notifications save(Notifications notification) {
       /* Notifications saved = notificationRepository.save(notification);

        // This guarantees that the saved notification (with id, timestamp, etc) is sent
        String username = saved.getRecipient().getUsername(); // Make sure this is unique and used as Principal in WebSocket
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                saved
        );
*/
        return null;
    }

    public Notifications sendNotification(String userId,Notifications notification){
        //log.info("Sending web socket notification to {} with payload {}",userId,notification);
        Notifications saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/notifications",
                notification
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

    @Override
    public void markAllAsRead() {
        List<Notifications> notifications = notificationRepository.findAll();
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
