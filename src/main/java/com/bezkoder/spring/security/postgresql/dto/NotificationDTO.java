package com.bezkoder.spring.security.postgresql.dto;

public record NotificationDTO(
        Long id,
        String recipientUsername,
        String message,
        String type,
        String createdAtIso,
        Boolean isRead
) {}