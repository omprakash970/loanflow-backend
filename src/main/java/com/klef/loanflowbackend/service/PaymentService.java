package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.PaymentDTO;
import com.klef.loanflowbackend.entity.Borrower;
import com.klef.loanflowbackend.entity.EmiSchedule;
import com.klef.loanflowbackend.entity.Lender;
import com.klef.loanflowbackend.entity.Loan;
import com.klef.loanflowbackend.entity.Payment;
import com.klef.loanflowbackend.entity.PaymentMethod;
import com.klef.loanflowbackend.entity.PaymentStatus;
import com.klef.loanflowbackend.repository.BorrowerRepository;
import com.klef.loanflowbackend.repository.EmiScheduleRepository;
import com.klef.loanflowbackend.repository.LenderRepository;
import com.klef.loanflowbackend.repository.LoanRepository;
import com.klef.loanflowbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LenderRepository lenderRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    public List<PaymentDTO> getPaymentsForLenderUser(Long lenderUserId) {
        Lender lender = lenderRepository.findByUserId(lenderUserId).orElse(null);
        if (lender == null) {
            return List.of();
        }

        // Simple approach: filter in-memory by lenderId through Loan relation.
        return paymentRepository.findAll().stream()
                .filter(p -> p.getLoan() != null && p.getLoan().getLender() != null && lender.getId().equals(p.getLoan().getLender().getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsForBorrowerUser(Long borrowerUserId) {
        Borrower borrower = borrowerRepository.findByUserId(borrowerUserId).orElse(null);
        if (borrower == null) {
            return List.of();
        }

        return paymentRepository.findAll().stream()
                .filter(p -> p.getLoan() != null && p.getLoan().getBorrower() != null && borrower.getId().equals(p.getLoan().getBorrower().getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO createPaymentFromBorrower(PaymentDTO paymentDTO, Long borrowerUserId) {
        try {
            log.info("Processing payment from borrower: {} for loan: {}", borrowerUserId, paymentDTO.getLoanId());

            Borrower borrower = borrowerRepository.findByUserId(borrowerUserId)
                    .orElseThrow(() -> new RuntimeException("Borrower not found"));

            Loan loan = loanRepository.findById(paymentDTO.getLoanId())
                    .orElseThrow(() -> new RuntimeException("Loan not found"));

            if (!loan.getBorrower().getId().equals(borrower.getId())) {
                throw new RuntimeException("Unauthorized: Loan does not belong to this borrower");
            }

            // Get EMI schedule if provided
            EmiSchedule emi = null;
            if (paymentDTO.getEmiScheduleId() != null) {
                emi = emiScheduleRepository.findById(paymentDTO.getEmiScheduleId())
                        .orElseThrow(() -> new RuntimeException("EMI Schedule not found"));
                emi.setStatus(PaymentStatus.COMPLETED);
                emiScheduleRepository.save(emi);
                log.info("EMI {} updated to COMPLETED", paymentDTO.getEmiScheduleId());
            }

            // Create payment record
            Payment payment = Payment.builder()
                    .paymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
                    .loan(loan)
                    .emiSchedule(emi)
                    .amount(paymentDTO.getAmount())
                    .paymentDate(System.currentTimeMillis())
                    .method(paymentDTO.getMethod() != null ? PaymentMethod.valueOf(paymentDTO.getMethod().toUpperCase()) : PaymentMethod.MANUAL)
                    .status(PaymentStatus.COMPLETED)
                    .build();

            Payment saved = paymentRepository.save(payment);
            log.info("Payment created successfully: {}", saved.getPaymentId());

            return toDTO(saved);
        } catch (Exception e) {
            log.error("Error creating payment from borrower: {}", e.getMessage(), e);
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private PaymentDTO toDTO(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .paymentId(p.getPaymentId())
                .loanId(p.getLoan() != null ? p.getLoan().getId() : null)
                .loanCode(p.getLoan() != null ? p.getLoan().getLoanId() : null)
                .borrowerName(p.getLoan() != null && p.getLoan().getBorrower() != null ? p.getLoan().getBorrower().getUser().getFullName() : null)
                .lenderName(p.getLoan() != null && p.getLoan().getLender() != null ? p.getLoan().getLender().getUser().getFullName() : null)
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .method(p.getMethod() != null ? p.getMethod().toString() : null)
                .status(p.getStatus() != null ? p.getStatus().toString() : null)
                .build();
    }
}

