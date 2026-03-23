package com.mktplace.listener;

import com.mktplace.observability.EventEnvelope;
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
    public void onDomainEvent(EventEnvelope envelope) {
        log.info("event_type={} routing_key={} trace_id={} payload_type={}", envelope.eventType(), envelope.routingKey(), envelope.traceId(), envelope.payload().getClass().getSimpleName());
        auditService.logAction("ASYNC_EVENT_" + envelope.eventType(), "EVENT", envelope.traceId(), envelope.routingKey());
    }
}
