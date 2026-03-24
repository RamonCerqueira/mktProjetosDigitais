package com.mktplace.events;

public record SubscriptionLifecycleEvent(Long subscriptionId, Long userId, String action, String status, boolean canPublish) {}
