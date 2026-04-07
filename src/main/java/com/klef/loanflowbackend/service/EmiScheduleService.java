package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.EmiScheduleDTO;
import com.klef.loanflowbackend.entity.EmiSchedule;
import com.klef.loanflowbackend.entity.Loan;
import com.klef.loanflowbackend.repository.EmiScheduleRepository;
import com.klef.loanflowbackend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmiScheduleService {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanRepository loanRepository;

    /**
     * Get EMI schedule by loan ID
     */
    public List<EmiScheduleDTO> getEmiScheduleByLoanId(Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new IllegalArgumentException("Loan not found with ID: " + loanId);
        }

        List<EmiSchedule> schedules = emiScheduleRepository.findByLoanIdOrderByMonthAsc(loanId);
        return schedules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all EMI schedules
     */
    public List<EmiScheduleDTO> getAllEmiSchedules() {
        List<EmiSchedule> schedules = emiScheduleRepository.findAll();
        return schedules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get EMI schedule by ID
     */
    public EmiScheduleDTO getEmiScheduleById(Long id) {
        EmiSchedule schedule = emiScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EMI Schedule not found with ID: " + id));
        return mapToDTO(schedule);
    }

    /**
     * Create EMI schedule
     */
    public EmiScheduleDTO createEmiSchedule(EmiScheduleDTO dto) {
        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + dto.getLoanId()));

        EmiSchedule schedule = new EmiSchedule();
        schedule.setLoan(loan);
        schedule.setMonth(dto.getMonth());
        schedule.setEmiAmount(dto.getEmiAmount());
        schedule.setPrincipal(dto.getPrincipal());
        schedule.setInterest(dto.getInterest());
        schedule.setBalance(dto.getBalance());

        EmiSchedule saved = emiScheduleRepository.save(schedule);
        return mapToDTO(saved);
    }

    /**
     * Update EMI schedule status
     */
    public EmiScheduleDTO updateEmiScheduleStatus(Long id, String status) {
        EmiSchedule schedule = emiScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EMI Schedule not found with ID: " + id));

        try {
            schedule.setStatus(com.klef.loanflowbackend.entity.PaymentStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        EmiSchedule updated = emiScheduleRepository.save(schedule);
        return mapToDTO(updated);
    }

    /**
     * Map EmiSchedule to DTO
     */
    private EmiScheduleDTO mapToDTO(EmiSchedule schedule) {
        return EmiScheduleDTO.builder()
                .id(schedule.getId())
                .loanId(schedule.getLoan().getId())
                .month(schedule.getMonth())
                .emiAmount(schedule.getEmiAmount())
                .principal(schedule.getPrincipal())
                .interest(schedule.getInterest())
                .balance(schedule.getBalance())
                .status(schedule.getStatus().name())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
