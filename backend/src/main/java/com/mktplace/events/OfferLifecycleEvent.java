package com.mktplace.events;

import java.math.BigDecimal;

public record OfferLifecycleEvent(Long offerId, Long projectId, Long buyerId, Long sellerId, Long actorId, Long recipientId, String action, BigDecimal amount) {}
