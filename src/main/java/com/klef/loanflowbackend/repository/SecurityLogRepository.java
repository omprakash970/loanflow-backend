package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.SecurityLog;
import com.klef.loanflowbackend.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long> {

    List<SecurityLog> findByOrderByTimestampDesc();

    List<SecurityLog> findBySeverity(RiskLevel severity);
}
