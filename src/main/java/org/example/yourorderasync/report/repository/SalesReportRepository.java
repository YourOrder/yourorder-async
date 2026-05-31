package org.example.yourorderasync.report.repository;

import org.example.yourorderasync.report.entity.SalesReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesReportRepository extends JpaRepository<SalesReportEntity, UUID> {

    List<SalesReportEntity> findByCompanyId(UUID companyId);

    Optional<SalesReportEntity> findByCompanyIdAndPeriodStartAndPeriodEnd(
            UUID companyId,
            LocalDate periodStart,
            LocalDate periodEnd
    );
}
