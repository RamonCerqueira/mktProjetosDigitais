package com.mktplace.observability;

import java.time.Instant;

public record EventEnvelope(String eventType, String routingKey, String traceId, Instant occurredAt, Object payload) {}
