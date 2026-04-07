package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.EmiScheduleDTO;
import com.klef.loanflowbackend.service.EmiScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi-schedule")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

    /**
     * Get EMI schedule for a specific loan
     * GET /api/emi-schedule/{loanId}
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<ApiResponse<List<EmiScheduleDTO>>> getEmiScheduleByLoanId(
            @PathVariable Long loanId) {
        try {
            List<EmiScheduleDTO> schedules = emiScheduleService.getEmiScheduleByLoanId(loanId);
            return ResponseEntity.ok(new ApiResponse<>(true, "EMI schedules retrieved successfully", schedules));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get all EMI schedules
     * GET /api/emi-schedule/list/all
     */
    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<EmiScheduleDTO>>> getAllEmiSchedules() {
        try {
            List<EmiScheduleDTO> schedules = emiScheduleService.getAllEmiSchedules();
            return ResponseEntity.ok(new ApiResponse<>(true, "All EMI schedules retrieved successfully", schedules));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get EMI schedule by ID
     * GET /api/emi-schedule/view/{id}
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<ApiResponse<EmiScheduleDTO>> getEmiScheduleById(@PathVariable Long id) {
        try {
            EmiScheduleDTO schedule = emiScheduleService.getEmiScheduleById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "EMI schedule retrieved successfully", schedule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Create new EMI schedule
     * POST /api/emi-schedule/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EmiScheduleDTO>> createEmiSchedule(@RequestBody EmiScheduleDTO dto) {
        try {
            EmiScheduleDTO created = emiScheduleService.createEmiSchedule(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "EMI schedule created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Update EMI schedule status
     * PUT /api/emi-schedule/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EmiScheduleDTO>> updateEmiScheduleStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            EmiScheduleDTO updated = emiScheduleService.updateEmiScheduleStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "EMI schedule status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }
}
