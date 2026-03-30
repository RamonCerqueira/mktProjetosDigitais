package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.SubscriptionResponse;
import com.mktplace.dto.AuthDtos.SubscriptionWebhookRequest;
import com.mktplace.service.SubscriptionService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserContextService userContextService;
    public SubscriptionController(SubscriptionService subscriptionService, UserContextService userContextService) { this.subscriptionService = subscriptionService; this.userContextService = userContextService; }
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping public SubscriptionResponse status() { return subscriptionService.getStatus(userContextService.getCurrentUser()); }
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/activate-mock") public SubscriptionResponse activateMock() { return subscriptionService.activateMockSubscription(userContextService.getCurrentUser()); }
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/cancel") public SubscriptionResponse cancel() { return subscriptionService.cancel(userContextService.getCurrentUser()); }
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/renew") public SubscriptionResponse renew() { return subscriptionService.renewNow(userContextService.getCurrentUser()); }
    @PostMapping("/webhook") public void webhook(@RequestBody SubscriptionWebhookRequest request) { subscriptionService.handleWebhook(request); }
}
