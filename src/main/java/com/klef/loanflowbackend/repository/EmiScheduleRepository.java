package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.EmiSchedule;
import com.klef.loanflowbackend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanId(Long loanId);

    List<EmiSchedule> findByLoanIdOrderByMonthAsc(Long loanId);

    List<EmiSchedule> findByStatus(PaymentStatus status);
}
