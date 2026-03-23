package com.mktplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String token;
    @ManyToOne(optional = false)
    private User user;
    private Instant expiresAt;
    private boolean revoked;
}
