package com.mktplace.service;

import com.mktplace.model.AuditLog;
import com.mktplace.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String action, String resourceType, String resourceId, String metadata) {
        auditLogRepository.save(AuditLog.builder()
                .actorEmail(currentActor())
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .metadata(metadata)
                .createdAt(Instant.now())
                .build());
    }

    public void logHttpRequest(HttpServletRequest request, int status) {
        auditLogRepository.save(AuditLog.builder()
                .actorEmail(currentActor())
                .action("HTTP_" + status)
                .httpMethod(request.getMethod())
                .path(request.getRequestURI())
                .ipAddress(request.getRemoteAddr())
                .metadata(request.getHeader("User-Agent"))
                .createdAt(Instant.now())
                .build());
    }

    private String currentActor() {
        var ctx = SecurityContextHolder.getContext().getAuthentication();
        return ctx == null ? "anonymous" : String.valueOf(ctx.getName());
    }
}
