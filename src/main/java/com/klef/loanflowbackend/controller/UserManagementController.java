package com.klef.loanflowbackend.controller;

import com.klef.loanflowbackend.dto.ApiResponse;
import com.klef.loanflowbackend.dto.UserDTO;
import com.klef.loanflowbackend.dto.UpdateUserStatusRequest;
import com.klef.loanflowbackend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * Get all users (Admin only)
     * GET /api/admin/users/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<UserDTO> users = userManagementService.getAllUsers(page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get all users without pagination (Admin only)
     * GET /api/admin/users/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<UserDTO> users = userManagementService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "All users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get user by ID (Admin only)
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userManagementService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get users by role (Admin only)
     * GET /api/admin/users/role/{role}
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByRole(@PathVariable String role) {
        try {
            List<UserDTO> users = userManagementService.getUsersByRole(role);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully by role", users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Update user status (Admin only)
     * PUT /api/admin/users/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request) {
        try {
            UserDTO updated = userManagementService.updateUserStatus(id, request.getStatus());
            return ResponseEntity.ok(new ApiResponse<>(true, "User status updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }

    /**
     * Get total user count (Admin only)
     * GET /api/admin/users/count/total
     */
    @GetMapping("/count/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalUserCount() {
        try {
            long count = userManagementService.getTotalUserCount();
            return ResponseEntity.ok(new ApiResponse<>(true, "Total user count retrieved", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error", e.getMessage()));
        }
    }
}
