package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.BorrowerSummaryDTO;
import com.klef.loanflowbackend.entity.Borrower;
import com.klef.loanflowbackend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowerSummaryService {

    private final BorrowerRepository borrowerRepository;

    public List<BorrowerSummaryDTO> getAllBorrowersSummary() {
        return borrowerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private BorrowerSummaryDTO toDTO(Borrower b) {
        return BorrowerSummaryDTO.builder()
                .borrowerId(b.getId())
                .userId(b.getUser() != null ? b.getUser().getId() : null)
                .idCode(b.getUser() != null ? "BRW-" + b.getUser().getId() : null)
                .name(b.getUser() != null ? b.getUser().getFullName() : null)
                .activeLoans(b.getActiveLoans())
                .riskLevel(b.getRiskLevel() != null ? b.getRiskLevel().toString() : null)
                .build();
    }
}

