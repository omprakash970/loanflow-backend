package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.Loan;
import com.klef.loanflowbackend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByLoanId(String loanId);

    List<Loan> findByBorrowerId(Long borrowerId);

    List<Loan> findByLenderId(Long lenderId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findAll();
}
