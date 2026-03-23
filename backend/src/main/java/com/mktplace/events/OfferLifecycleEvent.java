package com.mktplace.events;

import java.math.BigDecimal;

public record OfferLifecycleEvent(Long offerId, Long projectId, Long buyerId, Long sellerId, String action, BigDecimal amount) {}
