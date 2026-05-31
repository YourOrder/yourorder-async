package org.example.yourorderasync.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.notification.entity.NotificationEntity;
import org.example.yourorderasync.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationEntity sendNotification(UUID userId, UUID orderId, String message) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .orderId(orderId)
                .message(message)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification sent to userId={} for orderId={}: {}", userId, orderId, message);
        return notification;
    }
}
