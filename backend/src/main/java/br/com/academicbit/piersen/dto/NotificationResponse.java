package br.com.academicbit.piersen.dto;

import br.com.academicbit.piersen.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String message, LocalDateTime createdAt, boolean read) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getMessage(),
                notification.getCreatedAt(), notification.isRead());
    }
}
