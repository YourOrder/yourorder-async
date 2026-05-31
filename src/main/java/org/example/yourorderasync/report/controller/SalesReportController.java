package org.example.yourorderasync.report.controller;

import lombok.RequiredArgsConstructor;
import org.example.yourorderasync.report.dto.SalesReportResponse;
import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.example.yourorderasync.report.service.SalesReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;

    @GetMapping("/sales")
    public List<SalesReportResponse> getSalesReports(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period
    ) {
        return salesReportService.getReports(companyId, period)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SalesReportResponse toResponse(SalesReportEntity entity) {
        return new SalesReportResponse(
                entity.getId(),
                entity.getCompanyId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getTotalRevenue(),
                entity.getOrderCount(),
                entity.getGeneratedAt()
        );
    }
}
