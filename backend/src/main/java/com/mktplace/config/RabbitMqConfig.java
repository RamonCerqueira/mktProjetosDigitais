package com.mktplace.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "marketplace.events";
    public static final String DLX_EXCHANGE = "marketplace.events.dlx";
    public static final String AUDIT_QUEUE = "marketplace.audit";
    public static final String NOTIFICATION_QUEUE = "marketplace.notifications";
    public static final String INTEGRATION_QUEUE = "marketplace.integrations";
    public static final String NOTIFICATION_DLQ = "marketplace.notifications.dlq";

    @Bean
    public DirectExchange marketplaceExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange marketplaceDlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "notification.dlq")
                .build();
    }

    @Bean
    public Queue integrationQueue() {
        return QueueBuilder.durable(INTEGRATION_QUEUE).build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
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
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq()).to(marketplaceDlxExchange()).with("notification.dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory reliableRabbitListenerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer,
                                                                              ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(4)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        return template;
    }
}
