package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.DashboardResponse;
import com.mktplace.dto.AuthDtos.UserResponse;
import com.mktplace.service.OfferService;
import com.mktplace.service.ProjectService;
import com.mktplace.service.SubscriptionService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {
    private final UserContextService userContextService;
    private final SubscriptionService subscriptionService;
    private final ProjectService projectService;
    private final OfferService offerService;

    public DashboardController(UserContextService userContextService, SubscriptionService subscriptionService, ProjectService projectService, OfferService offerService) {
        this.userContextService = userContextService;
        this.subscriptionService = subscriptionService;
        this.projectService = projectService;
        this.offerService = offerService;
    }

    @GetMapping
    public DashboardResponse getDashboard() {
        var user = userContextService.getCurrentUser();
        return new DashboardResponse(new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getDocumentType(), user.getDocumentNumber()), subscriptionService.getStatus(user), projectService.myProjects(user), offerService.myOffers(user));
    }
}
