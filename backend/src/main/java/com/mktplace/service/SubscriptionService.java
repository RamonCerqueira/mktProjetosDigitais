package com.mktplace.service;

import com.mktplace.dto.AuthDtos.SubscriptionResponse;
import com.mktplace.enums.SubscriptionStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Subscription;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import com.mktplace.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ProjectRepository projectRepository;
    private final BigDecimal planPrice;
    private final int durationDays;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, ProjectRepository projectRepository, @Value("${app.subscription.plan-price}") BigDecimal planPrice, @Value("${app.subscription.duration-days}") int durationDays) {
        this.subscriptionRepository = subscriptionRepository;
        this.projectRepository = projectRepository;
        this.planPrice = planPrice;
        this.durationDays = durationDays;
    }

    public SubscriptionResponse getStatus(User user) {
        Subscription subscription = syncSubscription(user);
        return subscription == null ? new SubscriptionResponse(SubscriptionStatus.INACTIVE.name(), null, planPrice, false) : new SubscriptionResponse(subscription.getStatus().name(), subscription.getExpiresAt(), subscription.getPrice(), canPublish(user));
    }

    public SubscriptionResponse activateMockSubscription(User user) {
        Subscription subscription = subscriptionRepository.findByUser(user).orElse(Subscription.builder().user(user).price(planPrice).startedAt(Instant.now()).build());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plus(durationDays, ChronoUnit.DAYS));
        subscription.setUpdatedAt(Instant.now());
        subscription.setPrice(planPrice);
        subscriptionRepository.save(subscription);
        return getStatus(user);
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
        if (subscription.getExpiresAt() != null && subscription.getExpiresAt().isBefore(Instant.now()) && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.setStatus(SubscriptionStatus.PAST_DUE);
            subscription.setUpdatedAt(Instant.now());
            subscriptionRepository.save(subscription);
            projectRepository.findBySeller(user).forEach(project -> {
                project.setStatus(com.mktplace.enums.ProjectStatus.HIDDEN);
                projectRepository.save(project);
            });
        }
        return subscription;
    }
}
