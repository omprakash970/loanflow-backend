package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.RiskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskReportRepository extends JpaRepository<RiskReport, Long> {

    Optional<RiskReport> findByLoanId(Long loanId);
}
