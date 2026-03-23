package com.mktplace.service;

import com.mktplace.dto.AuthDtos.SubscriptionResponse;
import com.mktplace.dto.AuthDtos.SubscriptionWebhookRequest;
import com.mktplace.enums.SubscriptionStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Subscription;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import com.mktplace.repository.SubscriptionRepository;
import com.mktplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BigDecimal planPrice;
    private final int durationDays;
    private final AuditService auditService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, ProjectRepository projectRepository, UserRepository userRepository, AuditService auditService, @Value("${app.subscription.plan-price}") BigDecimal planPrice, @Value("${app.subscription.duration-days}") int durationDays) {
        this.subscriptionRepository = subscriptionRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.planPrice = planPrice;
        this.durationDays = durationDays;
    }

    public SubscriptionResponse getStatus(User user) {
        Subscription subscription = syncSubscription(user);
        return subscription == null ? new SubscriptionResponse(SubscriptionStatus.CANCELED.name(), null, planPrice, false, false, null) : toResponse(subscription, user);
    }

    public SubscriptionResponse activateMockSubscription(User user) {
        Subscription subscription = subscriptionRepository.findByUser(user).orElse(Subscription.builder().user(user).build());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        subscription.setUpdatedAt(Instant.now());
        subscription.setPrice(planPrice);
        subscription.setAutoRenew(true);
        subscription.setCanceledAt(null);
        subscription.setExternalReference(subscription.getExternalReference() == null ? UUID.randomUUID().toString() : subscription.getExternalReference());
        subscriptionRepository.save(subscription);
        auditService.logAction("SUBSCRIPTION_ACTIVATED", "SUBSCRIPTION", String.valueOf(subscription.getId()), user.getEmail());
        return toResponse(subscription, user);
    }

    public SubscriptionResponse cancel(User user) {
        Subscription subscription = subscriptionRepository.findByUser(user).orElseThrow(() -> new BusinessException("Assinatura não encontrada", HttpStatus.NOT_FOUND));
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setAutoRenew(false);
        subscription.setCanceledAt(Instant.now());
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
        hideProjects(user);
        auditService.logAction("SUBSCRIPTION_CANCELED", "SUBSCRIPTION", String.valueOf(subscription.getId()), user.getEmail());
        return toResponse(subscription, user);
    }

    public SubscriptionResponse renewNow(User user) {
        Subscription subscription = subscriptionRepository.findByUser(user).orElseThrow(() -> new BusinessException("Assinatura não encontrada", HttpStatus.NOT_FOUND));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(true);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        subscription.setUpdatedAt(Instant.now());
        subscription.setCanceledAt(null);
        subscription.setPrice(planPrice);
        subscriptionRepository.save(subscription);
        auditService.logAction("SUBSCRIPTION_RENEWED", "SUBSCRIPTION", String.valueOf(subscription.getId()), user.getEmail());
        return toResponse(subscription, user);
    }

    public void handleWebhook(SubscriptionWebhookRequest request) {
        Subscription subscription = resolveSubscription(request);
        switch (request.eventType()) {
            case "subscription.payment_failed" -> subscription.setStatus(SubscriptionStatus.PAST_DUE);
            case "subscription.canceled" -> {
                subscription.setStatus(SubscriptionStatus.CANCELED);
                subscription.setAutoRenew(false);
                subscription.setCanceledAt(Instant.now());
            }
            case "subscription.renewed", "subscription.activated" -> {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setAutoRenew(true);
                subscription.setExpiresAt(Instant.now().plus(durationDays, ChronoUnit.DAYS));
                subscription.setCanceledAt(null);
            }
            default -> throw new BusinessException("Evento de webhook não suportado", HttpStatus.BAD_REQUEST);
        }
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) hideProjects(subscription.getUser());
        auditService.logAction("SUBSCRIPTION_WEBHOOK_" + request.eventType(), "SUBSCRIPTION", String.valueOf(subscription.getId()), request.externalReference());
    }

    public boolean canPublish(User user) {
        Subscription subscription = syncSubscription(user);
        return subscription != null && subscription.getStatus() == SubscriptionStatus.ACTIVE && subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(Instant.now());
    }

    public void assertCanPublish(User user) {
        if (!canPublish(user)) throw new BusinessException("Assinatura ativa é obrigatória para publicar projetos", HttpStatus.FORBIDDEN);
    }

    public Subscription syncSubscription(User user) {
        var maybe = subscriptionRepository.findByUser(user);
        if (maybe.isEmpty()) return null;
        Subscription subscription = maybe.get();
        if (subscription.getExpiresAt() != null && subscription.getExpiresAt().isBefore(Instant.now())) {
            if (subscription.isAutoRenew() && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                subscription.setStartedAt(Instant.now());
                subscription.setExpiresAt(Instant.now().plus(durationDays, ChronoUnit.DAYS));
                subscription.setUpdatedAt(Instant.now());
                subscriptionRepository.save(subscription);
                auditService.logAction("SUBSCRIPTION_AUTO_RENEWED", "SUBSCRIPTION", String.valueOf(subscription.getId()), user.getEmail());
            } else if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                subscription.setUpdatedAt(Instant.now());
                subscriptionRepository.save(subscription);
                auditService.logAction("SUBSCRIPTION_PAST_DUE", "SUBSCRIPTION", String.valueOf(subscription.getId()), user.getEmail());
                hideProjects(user);
            }
        }
        return subscription;
    }

    private Subscription resolveSubscription(SubscriptionWebhookRequest request) {
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId()).orElseThrow(() -> new BusinessException("Usuário não encontrado para webhook", HttpStatus.NOT_FOUND));
            return subscriptionRepository.findByUser(user).orElseThrow(() -> new BusinessException("Assinatura não encontrada", HttpStatus.NOT_FOUND));
        }
        return subscriptionRepository.findByExternalReference(request.externalReference())
                .orElseThrow(() -> new BusinessException("Assinatura não encontrada para webhook", HttpStatus.NOT_FOUND));
    }

    private SubscriptionResponse toResponse(Subscription subscription, User user) {
        return new SubscriptionResponse(subscription.getStatus().name(), subscription.getExpiresAt(), subscription.getPrice(), canPublish(user), subscription.isAutoRenew(), subscription.getExternalReference());
    }

    private void hideProjects(User user) {
        projectRepository.findBySeller(user).forEach(project -> {
            project.setStatus(com.mktplace.enums.ProjectStatus.HIDDEN);
            projectRepository.save(project);
        });
    }
}
