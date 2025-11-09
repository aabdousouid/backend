package com.bezkoder.spring.security.postgresql.mappers;

import com.bezkoder.spring.security.postgresql.dto.NotificationDTO;
import com.bezkoder.spring.security.postgresql.models.Notifications;

public class NotificationMapper {
    public static NotificationDTO toDto(Notifications n) {
        return new NotificationDTO(
                n.getNotificationId(),
                n.getRecipient() != null ? n.getRecipient().getUsername() : null,
                n.getMessage(),
                n.getType() != null ? n.getType().name() : null,
                n.getCreatedAt() != null ? n.getCreatedAt().toInstant().toString() : null,
                n.getIsRead()
        );
    }
}