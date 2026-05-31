package org.example.yourorderasync.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationSentEvent(
        UUID orderId,
        UUID userId,
        String message,
        LocalDateTime sentAt
) {}
