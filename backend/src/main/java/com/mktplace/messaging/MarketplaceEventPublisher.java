package com.mktplace.messaging;

import com.mktplace.config.RabbitMqConfig;
import com.mktplace.observability.EventEnvelope;
import com.mktplace.observability.TraceContextFilter;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MarketplaceEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    public MarketplaceEventPublisher(ApplicationEventPublisher applicationEventPublisher, RabbitTemplate rabbitTemplate) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Object event, String routingKey) {
        EventEnvelope envelope = new EventEnvelope(
                event.getClass().getSimpleName(),
                routingKey,
                MDC.get(TraceContextFilter.TRACE_ID),
                Instant.now(),
                event
        );
        applicationEventPublisher.publishEvent(envelope);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, envelope);
    }
}
