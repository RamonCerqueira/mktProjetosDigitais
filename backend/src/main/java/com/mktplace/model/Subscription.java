package com.mktplace.model;

import com.mktplace.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    private User user;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    private BigDecimal price;
    private Instant startedAt;
    private Instant expiresAt;
    private Instant updatedAt;
    private Instant canceledAt;
    private boolean autoRenew;
    private String externalReference;
}
