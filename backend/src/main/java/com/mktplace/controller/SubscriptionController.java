package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.SubscriptionResponse;
import com.mktplace.service.SubscriptionService;
import com.mktplace.service.UserContextService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserContextService userContextService;
    public SubscriptionController(SubscriptionService subscriptionService, UserContextService userContextService) { this.subscriptionService = subscriptionService; this.userContextService = userContextService; }
    @GetMapping public SubscriptionResponse status() { return subscriptionService.getStatus(userContextService.getCurrentUser()); }
    @PostMapping("/activate-mock") public SubscriptionResponse activateMock() { return subscriptionService.activateMockSubscription(userContextService.getCurrentUser()); }
}
