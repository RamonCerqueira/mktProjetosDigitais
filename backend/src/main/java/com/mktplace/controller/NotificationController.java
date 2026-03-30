package com.mktplace.controller;

import com.mktplace.dto.NotificationDtos.NotificationListResponse;
import com.mktplace.dto.NotificationDtos.NotificationResponse;
import com.mktplace.dto.NotificationDtos.UnreadCountResponse;
import com.mktplace.service.NotificationService;
import com.mktplace.service.UserContextService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserContextService userContextService;

    public NotificationController(NotificationService notificationService, UserContextService userContextService) {
        this.notificationService = notificationService;
        this.userContextService = userContextService;
    }

    @GetMapping
    public NotificationListResponse list(@RequestParam(defaultValue = "20") int limit) {
        return notificationService.list(userContextService.getCurrentUser(), limit);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(notificationService.unreadCount(userContextService.getCurrentUser()));
    }

    @PostMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(@PathVariable Long notificationId) {
        return notificationService.markAsRead(userContextService.getCurrentUser(), notificationId);
    }

    @PostMapping("/read-all")
    public UnreadCountResponse markAllAsRead() {
        notificationService.markAllAsRead(userContextService.getCurrentUser());
        return new UnreadCountResponse(0);
    }
}
