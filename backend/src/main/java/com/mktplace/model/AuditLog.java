package com.mktplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actorEmail;
    private String action;
    private String resourceType;
    private String resourceId;
    private String httpMethod;
    private String path;
    private String ipAddress;
    @Column(length = 2000)
    private String metadata;
    private Instant createdAt;
}
