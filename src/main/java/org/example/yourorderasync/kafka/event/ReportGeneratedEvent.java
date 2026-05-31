package org.example.yourorderasync.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReportGeneratedEvent(
        UUID reportId,
        UUID companyId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal totalRevenue,
        Integer orderCount
) {}
