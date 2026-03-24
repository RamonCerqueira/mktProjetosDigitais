package com.mktplace.repository;

import com.mktplace.model.Offer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    @EntityGraph(attributePaths = {"buyer", "seller", "project", "proposer"})
    List<Offer> findByBuyerIdOrSellerIdOrderByUpdatedAtDesc(Long buyerId, Long sellerId);
    List<Offer> findByNegotiationKeyOrderByCreatedAtAsc(String negotiationKey);
}
