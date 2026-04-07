package com.klef.loanflowbackend.repository;

import com.klef.loanflowbackend.entity.LoanOffer;
import com.klef.loanflowbackend.entity.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanOfferRepository extends JpaRepository<LoanOffer, Long> {
    List<LoanOffer> findByStatus(OfferStatus status);
    List<LoanOffer> findByLenderId(Long lenderId);
}

