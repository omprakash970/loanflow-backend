package com.klef.loanflowbackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.loanflowbackend.entity.Role;
import com.klef.loanflowbackend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndRole(String email, Role role);

    Page<User> findAll(Pageable pageable);

    List<User> findByRole(Role role);
}