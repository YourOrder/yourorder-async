package org.example.yourorderasync.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        Integer quantity,
        BigDecimal price
) {}
