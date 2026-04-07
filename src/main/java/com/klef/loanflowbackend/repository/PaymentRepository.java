package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.Payment;
import com.klef.loanflowbackend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByLoanId(Long loanId);

    List<Payment> findByStatus(PaymentStatus status);
}
