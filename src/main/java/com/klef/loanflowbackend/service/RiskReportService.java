package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.RiskReportDTO;
import com.klef.loanflowbackend.entity.RiskReport;
import com.klef.loanflowbackend.repository.RiskReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskReportService {

    private final RiskReportRepository riskReportRepository;

    public List<RiskReportDTO> getAllRiskReports() {
        return riskReportRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private RiskReportDTO toDTO(RiskReport r) {
        Long loanPk = r.getLoan() != null ? r.getLoan().getId() : null;
        return RiskReportDTO.builder()
                .id(r.getId())
                .loanId(loanPk)
                .loanCode(r.getLoan() != null ? r.getLoan().getLoanId() : null)
                .riskScore(r.getRiskScore())
                .defaultProbability(r.getDefaultProbability())
                .build();
    }
}
