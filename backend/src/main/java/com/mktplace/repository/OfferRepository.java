package com.mktplace.repository;

import com.mktplace.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);
}
