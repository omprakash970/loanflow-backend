package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.dto.UserDTO;
import com.klef.loanflowbackend.entity.Role;
import com.klef.loanflowbackend.entity.User;
import com.klef.loanflowbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * Get all users (paginated)
     */
    public List<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all users
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        return mapToDTO(user);
    }

    /**
     * Get user by email
     */
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return mapToDTO(user);
    }

    /**
     * Get all users by role
     */
    public List<UserDTO> getUsersByRole(String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            // You may need to add a method to UserRepository to filter by role
            List<User> users = userRepository.findAll();
            return users.stream()
                    .filter(u -> u.getRole() == roleEnum)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Update user status (ACTIVE, DISABLED)
     */
    public UserDTO updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("DISABLED")) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE or DISABLED");
        }

        // You may need to add a status field to User entity if not already present
        // For now, this is a placeholder. You should add a status field to the User entity.
        
        return mapToDTO(user);
    }

    /**
     * Get total user count
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }

    /**
     * Map User to DTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
