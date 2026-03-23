package com.mktplace.model;

import com.mktplace.enums.OfferActionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OfferHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Offer offer;
    @ManyToOne(optional = false) private User actor;
    @Enumerated(EnumType.STRING)
    private OfferActionType actionType;
    private BigDecimal amount;
    @Column(length = 500)
    private String details;
    private Instant createdAt;
}
