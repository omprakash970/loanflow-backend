package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.LoanDTO;
import com.klef.loanflowbackend.dto.LoanOfferDTO;
import com.klef.loanflowbackend.dto.LoanRequestDTO;
import com.klef.loanflowbackend.entity.*;
import com.klef.loanflowbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final LoanOfferRepository loanOfferRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;
    private final UserRepository userRepository;

    // --- Offers ---

    public LoanOfferDTO createOffer(Long lenderUserId, LoanOfferDTO dto) {
        // Get user and create lender profile if needed
        User user = userRepository.findById(lenderUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lender lender = lenderRepository.findByUserId(lenderUserId)
                .orElseGet(() -> lenderRepository.save(
                        Lender.builder()
                                .user(user)
                                .companyName(null)
                                .activeLoans(0)
                                .totalDisbursed(0.0)
                                .createdAt(System.currentTimeMillis())
                                .build()
                ));

        if (dto.getMinAmount() == null || dto.getMaxAmount() == null || dto.getMinAmount() <= 0 || dto.getMaxAmount() <= 0 || dto.getMaxAmount() < dto.getMinAmount()) {
            throw new IllegalArgumentException("Invalid amount range");
        }
        if (dto.getInterestRate() == null || dto.getInterestRate() <= 0) {
            throw new IllegalArgumentException("Invalid interest rate");
        }
        if (dto.getTenure() == null || dto.getTenure() <= 0) {
            throw new IllegalArgumentException("Invalid tenure");
        }

        LoanOffer offer = LoanOffer.builder()
                .offerCode("OFF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .lender(lender)
                .minAmount(dto.getMinAmount())
                .maxAmount(dto.getMaxAmount())
                .interestRate(dto.getInterestRate())
                .tenure(dto.getTenure())
                .status(OfferStatus.OPEN)
                .build();

        return toOfferDTO(loanOfferRepository.save(offer));
    }

    public List<LoanOfferDTO> listOpenOffers() {
        return loanOfferRepository.findByStatus(OfferStatus.OPEN).stream().map(this::toOfferDTO).collect(Collectors.toList());
    }

    public List<LoanOfferDTO> listMyOffers(Long lenderUserId) {
        Lender lender = lenderRepository.findByUserId(lenderUserId)
                .orElse(null);

        if (lender == null) {
            return List.of();
        }

        return loanOfferRepository.findByLenderId(lender.getId()).stream().map(this::toOfferDTO).collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO acceptOffer(Long borrowerUserId, Long offerId, Double amount, String purpose) {
        // Get user and create borrower profile if needed
        User user = userRepository.findById(borrowerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Borrower borrower = borrowerRepository.findByUserId(borrowerUserId)
                .orElseGet(() -> borrowerRepository.save(
                        Borrower.builder()
                                .user(user)
                                .activeLoans(0)
                                .riskLevel(RiskLevel.LOW)
                                .creditScore(700)
                                .kycVerified(Boolean.FALSE)
                                .createdAt(System.currentTimeMillis())
                                .build()
                ));

        LoanOffer offer = loanOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new IllegalStateException("Offer is not open");
        }
        if (amount == null || amount < offer.getMinAmount() || amount > offer.getMaxAmount()) {
            throw new IllegalArgumentException("Amount must be within offer range");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setAcceptedByBorrower(borrower);

        Loan loan = Loan.builder()
                .loanId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .borrower(borrower)
                .lender(offer.getLender())
                .amount(amount)
                .interestRate(offer.getInterestRate())
                .tenure(offer.getTenure())
                .purpose(purpose != null ? purpose : "Offer Acceptance")
                .status(LoanStatus.ACTIVE)
                .startDate(System.currentTimeMillis())
                .nextPaymentDate(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
                .build();

        Loan savedLoan = loanRepository.save(loan);
        offer.setSanctionedLoan(savedLoan);
        loanOfferRepository.save(offer);

        return toLoanDTO(savedLoan);
    }

    // --- Requests ---

    public LoanRequestDTO createRequest(Long borrowerUserId, LoanRequestDTO dto) {
        // Get user to ensure they exist
        User user = userRepository.findById(borrowerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Try to find existing borrower profile, create if doesn't exist
        Borrower borrower = borrowerRepository.findByUserId(borrowerUserId)
                .orElseGet(() -> borrowerRepository.save(
                        Borrower.builder()
                                .user(user)
                                .activeLoans(0)
                                .riskLevel(RiskLevel.LOW)
                                .creditScore(700)
                                .kycVerified(Boolean.FALSE)
                                .createdAt(System.currentTimeMillis())
                                .build()
                ));

        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (dto.getTenure() == null || dto.getTenure() <= 0) {
            throw new IllegalArgumentException("Invalid tenure");
        }
        if (dto.getPurpose() == null || dto.getPurpose().isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }

        LoanRequest req = LoanRequest.builder()
                .requestCode("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .borrower(borrower)
                .amount(dto.getAmount())
                .tenure(dto.getTenure())
                .purpose(dto.getPurpose())
                .interestRate(dto.getInterestRate() != null ? dto.getInterestRate() : 10.5)
                .status(RequestStatus.OPEN)
                .build();

        return toRequestDTO(loanRequestRepository.save(req));
    }

    public List<LoanRequestDTO> listOpenRequests() {
        return loanRequestRepository.findByStatus(RequestStatus.OPEN).stream().map(this::toRequestDTO).collect(Collectors.toList());
    }

    public List<LoanRequestDTO> listMyRequests(Long borrowerUserId) {
        Borrower borrower = borrowerRepository.findByUserId(borrowerUserId)
                .orElseThrow(() -> new RuntimeException("Borrower profile not found"));
        return loanRequestRepository.findByBorrowerId(borrower.getId()).stream().map(this::toRequestDTO).collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO approveRequest(Long lenderUserId, Long requestId) {
        // Get user and create lender profile if needed
        User user = userRepository.findById(lenderUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Lender lender = lenderRepository.findByUserId(lenderUserId)
                .orElseGet(() -> lenderRepository.save(
                        Lender.builder()
                                .user(user)
                                .companyName(null)
                                .activeLoans(0)
                                .totalDisbursed(0.0)
                                .createdAt(System.currentTimeMillis())
                                .build()
                ));

        LoanRequest req = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (req.getStatus() != RequestStatus.OPEN) {
            throw new IllegalStateException("Request is not open");
        }

        req.setStatus(RequestStatus.APPROVED);
        req.setApprovedByLender(lender);

        Loan loan = Loan.builder()
                .loanId("LOAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .borrower(req.getBorrower())
                .lender(lender)
                .amount(req.getAmount())
                .interestRate(req.getInterestRate())
                .tenure(req.getTenure())
                .purpose(req.getPurpose())
                .status(LoanStatus.ACTIVE)
                .startDate(System.currentTimeMillis())
                .nextPaymentDate(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
                .build();

        Loan savedLoan = loanRepository.save(loan);
        req.setSanctionedLoan(savedLoan);
        loanRequestRepository.save(req);

        return toLoanDTO(savedLoan);
    }

    // --- Mapping helpers ---

    private LoanOfferDTO toOfferDTO(LoanOffer o) {
        return LoanOfferDTO.builder()
                .id(o.getId())
                .offerCode(o.getOfferCode())
                .lenderId(o.getLender() != null ? o.getLender().getId() : null)
                .lenderName(o.getLender() != null ? o.getLender().getUser().getFullName() : null)
                .minAmount(o.getMinAmount())
                .maxAmount(o.getMaxAmount())
                .interestRate(o.getInterestRate())
                .tenure(o.getTenure())
                .status(o.getStatus() != null ? o.getStatus().toString() : null)
                .acceptedByBorrowerId(o.getAcceptedByBorrower() != null ? o.getAcceptedByBorrower().getId() : null)
                .acceptedByBorrowerName(o.getAcceptedByBorrower() != null ? o.getAcceptedByBorrower().getUser().getFullName() : null)
                .sanctionedLoanId(o.getSanctionedLoan() != null ? o.getSanctionedLoan().getId() : null)
                .sanctionedLoanCode(o.getSanctionedLoan() != null ? o.getSanctionedLoan().getLoanId() : null)
                .createdAt(o.getCreatedAt())
                .build();
    }

    private LoanRequestDTO toRequestDTO(LoanRequest r) {
        return LoanRequestDTO.builder()
                .id(r.getId())
                .requestCode(r.getRequestCode())
                .borrowerId(r.getBorrower() != null ? r.getBorrower().getId() : null)
                .borrowerName(r.getBorrower() != null ? r.getBorrower().getUser().getFullName() : null)
                .amount(r.getAmount())
                .tenure(r.getTenure())
                .purpose(r.getPurpose())
                .interestRate(r.getInterestRate())
                .status(r.getStatus() != null ? r.getStatus().toString() : null)
                .approvedByLenderId(r.getApprovedByLender() != null ? r.getApprovedByLender().getId() : null)
                .approvedByLenderName(r.getApprovedByLender() != null ? r.getApprovedByLender().getUser().getFullName() : null)
                .sanctionedLoanId(r.getSanctionedLoan() != null ? r.getSanctionedLoan().getId() : null)
                .sanctionedLoanCode(r.getSanctionedLoan() != null ? r.getSanctionedLoan().getLoanId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private LoanDTO toLoanDTO(Loan loan) {
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

