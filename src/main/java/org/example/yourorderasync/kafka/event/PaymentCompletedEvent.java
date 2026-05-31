package org.example.yourorderasync.kafka.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId
) {}
