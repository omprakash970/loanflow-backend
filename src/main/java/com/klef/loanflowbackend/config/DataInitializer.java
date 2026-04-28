package com.klef.loanflowbackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.klef.loanflowbackend.entity.Borrower;
import com.klef.loanflowbackend.entity.EmiSchedule;
import com.klef.loanflowbackend.entity.Lender;
import com.klef.loanflowbackend.entity.Loan;
import com.klef.loanflowbackend.entity.LoanStatus;
import com.klef.loanflowbackend.entity.PaymentStatus;
import com.klef.loanflowbackend.entity.Role;
import com.klef.loanflowbackend.entity.RiskLevel;
import com.klef.loanflowbackend.entity.User;
import com.klef.loanflowbackend.repository.BorrowerRepository;
import com.klef.loanflowbackend.repository.EmiScheduleRepository;
import com.klef.loanflowbackend.repository.LenderRepository;
import com.klef.loanflowbackend.repository.LoanRepository;
import com.klef.loanflowbackend.repository.LoanOfferRepository;
import com.klef.loanflowbackend.repository.LoanRequestRepository;
import com.klef.loanflowbackend.repository.PaymentRepository;
import com.klef.loanflowbackend.repository.RiskReportRepository;
import com.klef.loanflowbackend.repository.SecurityLogRepository;
import com.klef.loanflowbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            BorrowerRepository borrowerRepository,
            LenderRepository lenderRepository,
            LoanRepository loanRepository,
            LoanRequestRepository loanRequestRepository,
            LoanOfferRepository loanOfferRepository,
            EmiScheduleRepository emiScheduleRepository,
            PaymentRepository paymentRepository,
            RiskReportRepository riskReportRepository,
            SecurityLogRepository securityLogRepository) {

        return args -> {

            System.out.println("Clearing existing data...");

            securityLogRepository.deleteAll();
            riskReportRepository.deleteAll();
            paymentRepository.deleteAll();
            emiScheduleRepository.deleteAll();
            loanOfferRepository.deleteAll();
            loanRequestRepository.deleteAll();
            loanRepository.deleteAll();
            lenderRepository.deleteAll();
            borrowerRepository.deleteAll();
            userRepository.deleteAll();

            System.out.println("✓ All data cleared");

            // Admin
            User adminUser = User.builder()
                    .fullName("System Admin")
                    .email("admin@loanflow.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(adminUser);

            System.out.println("✓ Admin account created: admin@loanflow.com / admin123");

            // Borrower
            User borrowerUser = User.builder()
                    .fullName("John Doe")
                    .email("borrower@loanflow.com")
                    .password(passwordEncoder.encode("borrower123"))
                    .role(Role.BORROWER)
                    .build();

            userRepository.save(borrowerUser);

            Borrower borrower = Borrower.builder()
                    .user(borrowerUser)
                    .activeLoans(1)
                    .creditScore(750)
                    .kycVerified(true)
                    .riskLevel(RiskLevel.LOW)
                    .createdAt(System.currentTimeMillis())
                    .build();

            borrowerRepository.save(borrower);

            System.out.println("✓ Test borrower created: borrower@loanflow.com / borrower123");

            // Lender
            User lenderUser = User.builder()
                    .fullName("ABC Bank")
                    .email("lender@loanflow.com")
                    .password(passwordEncoder.encode("lender123"))
                    .role(Role.LENDER)
                    .build();

            userRepository.save(lenderUser);

            Lender lender = Lender.builder()
                    .user(lenderUser)
                    .companyName("ABC Bank Ltd")
                    .totalDisbursed(50000.0)
                    .activeLoans(1)
                    .createdAt(System.currentTimeMillis())
                    .build();

            lenderRepository.save(lender);

            System.out.println("✓ Test lender created: lender@loanflow.com / lender123");

            // Loan
            Loan loan = Loan.builder()
                    .loanId("LOAN-TEST001")
                    .borrower(borrower)
                    .lender(lender)
                    .amount(50000.0)
                    .interestRate(12.0)
                    .tenure(24)
                    .purpose("Home Renovation")
                    .status(LoanStatus.ACTIVE)
                    .startDate(System.currentTimeMillis())
                    .nextPaymentDate(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
                    .createdAt(System.currentTimeMillis())
                    .build();

            loanRepository.save(loan);

            System.out.println("✓ Test loan created: LOAN-TEST001");

            // EMI Schedule
            double principal = loan.getAmount();
            double annualRate = loan.getInterestRate() / 100.0;
            double monthlyRate = annualRate / 12.0;
            int tenure = loan.getTenure();

            double emiAmount;

            if (monthlyRate == 0) {
                emiAmount = principal / tenure;
            } else {
                double factor = Math.pow(1 + monthlyRate, tenure);
                emiAmount = (principal * monthlyRate * factor) / (factor - 1);
            }

            double remainingBalance = principal;

            for (int month = 1; month <= tenure; month++) {

                double interestAmount = remainingBalance * monthlyRate;
                double principalAmount = emiAmount - interestAmount;

                remainingBalance -= principalAmount;

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

            System.out.println("✓ EMI schedules generated: 24 monthly EMIs");
            System.out.println("✓ DataInitializer completed");
        };
    }
}