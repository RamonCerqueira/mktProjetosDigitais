package com.mktplace.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "marketplace.events";
    public static final String AUDIT_QUEUE = "marketplace.audit";
    public static final String NOTIFICATION_QUEUE = "marketplace.notifications";
    public static final String INTEGRATION_QUEUE = "marketplace.integrations";

    @Bean
    public DirectExchange marketplaceExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue integrationQueue() {
        return QueueBuilder.durable(INTEGRATION_QUEUE).build();
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue()).to(marketplaceExchange()).with("audit");
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(marketplaceExchange()).with("notification");
    }

    @Bean
    public Binding integrationBinding() {
        return BindingBuilder.bind(integrationQueue()).to(marketplaceExchange()).with("integration");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
