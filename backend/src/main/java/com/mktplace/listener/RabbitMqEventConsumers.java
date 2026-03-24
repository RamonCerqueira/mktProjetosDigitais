package com.mktplace.listener;

import com.mktplace.config.RabbitMqConfig;
import com.mktplace.observability.EventEnvelope;
import com.mktplace.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventConsumers {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventConsumers.class);
    private final NotificationService notificationService;

    public RabbitMqEventConsumers(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMqConfig.AUDIT_QUEUE, containerFactory = "reliableRabbitListenerFactory")
    public void consumeAuditEvent(EventEnvelope envelope) {
        log.info("queue=audit trace_id={} event_type={} payload={}", envelope.traceId(), envelope.eventType(), envelope.payload());
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE, containerFactory = "reliableRabbitListenerFactory")
    public void consumeNotificationEvent(EventEnvelope envelope) {
        log.info("queue=notification trace_id={} event_type={} payload={}", envelope.traceId(), envelope.eventType(), envelope.payload());
        notificationService.processEnvelope(envelope);
    }

    @RabbitListener(queues = RabbitMqConfig.INTEGRATION_QUEUE, containerFactory = "reliableRabbitListenerFactory")
    public void consumeIntegrationEvent(EventEnvelope envelope) {
        log.info("queue=integration trace_id={} event_type={} payload={}", envelope.traceId(), envelope.eventType(), envelope.payload());
    }
}
