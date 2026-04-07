package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.LoanDTO;
import com.klef.loanflowbackend.entity.*;
import com.klef.loanflowbackend.repository.BorrowerRepository;
import com.klef.loanflowbackend.repository.EmiScheduleRepository;
import com.klef.loanflowbackend.repository.LenderRepository;
import com.klef.loanflowbackend.repository.LoanRepository;
import com.klef.loanflowbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final UserRepository userRepository;
    private final LenderRepository lenderRepository;
    private final EmiScheduleRepository emiScheduleRepository;

    /**
     * Create a new loan application
     */
    public LoanDTO applyForLoan(String userId, LoanDTO loanDTO) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Borrower borrower = borrowerRepository.findByUserId(user.getId())
                .orElse(Borrower.builder()
                        .user(user)
                        .activeLoans(0)
                        .riskLevel(RiskLevel.LOW)
                        .creditScore(700)
                        .kycVerified(Boolean.FALSE)
                        .createdAt(System.currentTimeMillis())
                        .build());

        Loan loan = Loan.builder()
                .loanId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .borrower(borrower)
                .amount(loanDTO.getAmount())
                .interestRate(loanDTO.getInterestRate() != null ? loanDTO.getInterestRate() : 10.5)
                .tenure(loanDTO.getTenure())
                .purpose(loanDTO.getPurpose())
                .status(LoanStatus.PENDING)
                .createdAt(System.currentTimeMillis())
                .build();

        Loan savedLoan = loanRepository.save(loan);
        return mapToDTO(savedLoan);
    }

    /**
     * Get all pending loans (for admin review)
     */
    public List<LoanDTO> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.PENDING)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LoanDTO> getBorrowerLoans(String userId) {
        Long borrowerId = Long.parseLong(userId);
        Borrower borrower = borrowerRepository.findByUserId(borrowerId)
                .orElse(null);

        if (borrower == null) {
            return List.of();
        }

        return loanRepository.findByBorrowerId(borrower.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Approve a loan application and generate EMI schedules
     */
    public LoanDTO approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(System.currentTimeMillis());
        loan.setNextPaymentDate(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 days from now
        loan.setUpdatedAt(System.currentTimeMillis());

        Loan updated = loanRepository.save(loan);

        // Generate EMI schedules based on loan tenure
        generateEmiSchedules(updated);

        return mapToDTO(updated);
    }

    /**
     * Generate EMI schedules for a loan
     * Calculates monthly EMI using the standard amortization formula
     */
    private void generateEmiSchedules(Loan loan) {
        // Clear any existing EMI schedules for this loan
        List<EmiSchedule> existing = emiScheduleRepository.findByLoanIdOrderByMonthAsc(loan.getId());
        emiScheduleRepository.deleteAll(existing);

        // EMI Calculation: EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
        // Where: P = Principal, r = monthly interest rate, n = tenure in months
        double principal = loan.getAmount();
        double annualRate = loan.getInterestRate() / 100.0;
        double monthlyRate = annualRate / 12.0;
        int tenure = loan.getTenure(); // in months

        // Calculate monthly EMI
        double emiAmount;
        if (monthlyRate == 0) {
            emiAmount = principal / tenure;
        } else {
            double factor = Math.pow(1 + monthlyRate, tenure);
            emiAmount = (principal * monthlyRate * factor) / (factor - 1);
        }

        // Generate EMI schedule for each month
        double remainingBalance = principal;
        for (int month = 1; month <= tenure; month++) {
            double interestAmount = remainingBalance * monthlyRate;
            double principalAmount = emiAmount - interestAmount;
            remainingBalance -= principalAmount;

            // Handle rounding for last month
            if (month == tenure) {
                principalAmount = principal - (emiAmount * (tenure - 1) -
                    emiScheduleRepository.findByLoanIdOrderByMonthAsc(loan.getId())
                        .stream()
                        .mapToDouble(EmiSchedule::getPrincipal)
                        .sum());
                interestAmount = emiAmount - principalAmount;
                remainingBalance = 0;
            }

            EmiSchedule schedule = EmiSchedule.builder()
                    .loan(loan)
                    .month(month)
                    .emiAmount(Math.round(emiAmount * 100.0) / 100.0)
                    .principal(Math.round(principalAmount * 100.0) / 100.0)
                    .interest(Math.round(interestAmount * 100.0) / 100.0)
                    .balance(Math.max(0, Math.round(remainingBalance * 100.0) / 100.0))
                    .status(PaymentStatus.UPCOMING)
                    .createdAt(System.currentTimeMillis())
                    .build();

            emiScheduleRepository.save(schedule);
        }
    }

    /**
     * Reject a loan application
     */
    public LoanDTO rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        loan.setUpdatedAt(System.currentTimeMillis());

        Loan updated = loanRepository.save(loan);
        return mapToDTO(updated);
    }

    /**
     * Get loan by ID
     */
    public LoanDTO getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return mapToDTO(loan);
    }

    /**
     * Get all loans (admin/analyst)
     */
    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get lender loans for the logged-in lender user
     */
    public List<LoanDTO> getLenderLoans(String userId) {
        Long lenderUserId = Long.parseLong(userId);
        Lender lender = lenderRepository.findByUserId(lenderUserId)
                .orElse(null);
        if (lender == null) {
            return List.of();
        }
        return loanRepository.findByLenderId(lender.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map Loan entity to DTO
     */
    private LoanDTO mapToDTO(Loan loan) {
        return LoanDTO.builder()
                .id(loan.getId())
                .loanId(loan.getLoanId())
                .borrowerName(loan.getBorrower().getUser().getFullName())
                .borrowerEmail(loan.getBorrower().getUser().getEmail())
                .borrowerId(loan.getBorrower().getId())
                .lenderName(loan.getLender() != null ? loan.getLender().getUser().getFullName() : null)
                .lenderId(loan.getLender() != null ? loan.getLender().getId() : null)
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .tenure(loan.getTenure())
                .purpose(loan.getPurpose())
                .status(loan.getStatus().toString())
                .createdAt(loan.getCreatedAt())
                .startDate(loan.getStartDate())
                .nextPaymentDate(loan.getNextPaymentDate())
                .build();
    }
}
