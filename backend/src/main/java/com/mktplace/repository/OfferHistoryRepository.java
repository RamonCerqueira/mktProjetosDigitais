package com.mktplace.repository;

import com.mktplace.model.OfferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferHistoryRepository extends JpaRepository<OfferHistory, Long> {
    List<OfferHistory> findByOfferNegotiationKeyOrderByCreatedAtAsc(String negotiationKey);
}
