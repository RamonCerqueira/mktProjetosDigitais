package com.mktplace.dto;

import com.mktplace.enums.NotificationType;

import java.time.Instant;
import java.util.List;

public class NotificationDtos {
    public record NotificationResponse(Long id, NotificationType type, String title, String body, Instant createdAt, Instant readAt) {}
    public record NotificationListResponse(List<NotificationResponse> items, long unreadCount) {}
    public record UnreadCountResponse(long unreadCount) {}
}
