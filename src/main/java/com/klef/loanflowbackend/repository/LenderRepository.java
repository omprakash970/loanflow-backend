package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LenderRepository extends JpaRepository<Lender, Long> {

    Optional<Lender> findByUserId(Long userId);
}
