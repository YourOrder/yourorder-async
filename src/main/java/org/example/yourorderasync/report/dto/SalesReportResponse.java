package org.example.yourorderasync.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesReportResponse(
        UUID id,
        UUID companyId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal totalRevenue,
        Integer orderCount,
        LocalDateTime generatedAt
) {}
