package com.mktplace.events;

import java.math.BigDecimal;

public record TransactionLifecycleEvent(Long transactionId, Long projectId, Long buyerId, Long sellerId, String action, BigDecimal amount, String status) {}
