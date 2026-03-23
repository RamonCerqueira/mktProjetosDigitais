package com.mktplace.listener;

import com.mktplace.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqEventConsumers {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventConsumers.class);

    @RabbitListener(queues = RabbitMqConfig.AUDIT_QUEUE)
    public void consumeAuditEvent(Object payload) {
        log.info("Audit queue event received: {}", payload);
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void consumeNotificationEvent(Object payload) {
        log.info("Notification queue event received: {}", payload);
    }

    @RabbitListener(queues = RabbitMqConfig.INTEGRATION_QUEUE)
    public void consumeIntegrationEvent(Object payload) {
        log.info("Integration queue event received: {}", payload);
    }
}
