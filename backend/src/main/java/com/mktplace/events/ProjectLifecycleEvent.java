package com.mktplace.events;

public record ProjectLifecycleEvent(Long projectId, Long sellerId, String action, String title) {}
