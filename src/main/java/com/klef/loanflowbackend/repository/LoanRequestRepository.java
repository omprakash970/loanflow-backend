package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.LoanRequest;
import com.klef.loanflowbackend.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByStatus(RequestStatus status);
    List<LoanRequest> findByBorrowerId(Long borrowerId);
}

