package com.mktplace.listener;

import com.mktplace.events.OfferLifecycleEvent;
import com.mktplace.events.ProjectLifecycleEvent;
import com.mktplace.events.SubscriptionLifecycleEvent;
import com.mktplace.events.TransactionLifecycleEvent;
import com.mktplace.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MarketplaceEventListener {
    private static final Logger log = LoggerFactory.getLogger(MarketplaceEventListener.class);
    private final AuditService auditService;

    public MarketplaceEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Async("marketplaceTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void onProjectEvent(ProjectLifecycleEvent event) {
        log.info("Processed project event {} for project {}", event.action(), event.projectId());
        auditService.logAction("ASYNC_PROJECT_" + event.action(), "PROJECT", String.valueOf(event.projectId()), event.title());
    }

    @Async("marketplaceTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void onOfferEvent(OfferLifecycleEvent event) {
        log.info("Processed offer event {} for offer {}", event.action(), event.offerId());
        auditService.logAction("ASYNC_OFFER_" + event.action(), "OFFER", String.valueOf(event.offerId()), "amount=" + event.amount());
    }

    @Async("marketplaceTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void onTransactionEvent(TransactionLifecycleEvent event) {
        log.info("Processed transaction event {} for transaction {}", event.action(), event.transactionId());
        auditService.logAction("ASYNC_TRANSACTION_" + event.action(), "TRANSACTION", String.valueOf(event.transactionId()), event.status());
    }

    @Async("marketplaceTaskExecutor")
    @TransactionalEventListener(fallbackExecution = true)
    public void onSubscriptionEvent(SubscriptionLifecycleEvent event) {
        log.info("Processed subscription event {} for subscription {}", event.action(), event.subscriptionId());
        auditService.logAction("ASYNC_SUBSCRIPTION_" + event.action(), "SUBSCRIPTION", String.valueOf(event.subscriptionId()), event.status());
    }
}
