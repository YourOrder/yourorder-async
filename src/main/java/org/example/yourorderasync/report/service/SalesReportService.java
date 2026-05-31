package org.example.yourorderasync.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.yourorderasync.kafka.event.OrderCreatedEvent;
import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.example.yourorderasync.report.repository.SalesReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SalesReportService {

    private final SalesReportRepository salesReportRepository;

    public SalesReportEntity updateReport(OrderCreatedEvent event, LocalDate period) {
        LocalDate periodStart = period.withDayOfMonth(1);
        LocalDate periodEnd = period.withDayOfMonth(period.lengthOfMonth());

        SalesReportEntity report = salesReportRepository
                .findByCompanyIdAndPeriodStartAndPeriodEnd(event.companyId(), periodStart, periodEnd)
                .orElseGet(() -> SalesReportEntity.builder()
                        .companyId(event.companyId())
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .build()
                );

        report.setTotalRevenue(report.getTotalRevenue().add(event.totalAmount()));
        report.setOrderCount(report.getOrderCount() + 1);

        SalesReportEntity saved = salesReportRepository.save(report);
        log.info("SalesReport updated: companyId={}, revenue={}, orders={}",
                event.companyId(), saved.getTotalRevenue(), saved.getOrderCount());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SalesReportEntity> getReports(UUID companyId, LocalDate period) {
        if (companyId != null && period != null) {
            LocalDate periodStart = period.withDayOfMonth(1);
            LocalDate periodEnd = period.withDayOfMonth(period.lengthOfMonth());
            return salesReportRepository
                    .findByCompanyIdAndPeriodStartAndPeriodEnd(companyId, periodStart, periodEnd)
                    .map(List::of)
                    .orElse(List.of());
        }
        if (companyId != null) {
            return salesReportRepository.findByCompanyId(companyId);
        }
        return salesReportRepository.findAll();
    }
}
