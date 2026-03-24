package com.mktplace.model;

import com.mktplace.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(indexes = {
        @Index(name = "idx_offer_buyer_updated", columnList = "buyer_id,updatedAt"),
        @Index(name = "idx_offer_seller_updated", columnList = "seller_id,updatedAt"),
        @Index(name = "idx_offer_negotiation_created", columnList = "negotiationKey,createdAt")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Offer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Project project;
    @ManyToOne(optional = false) private User buyer;
    @ManyToOne(optional = false) private User seller;
    @ManyToOne(optional = false) private User proposer;
    @ManyToOne private Offer parentOffer;
    @Column(nullable = false)
    private String negotiationKey;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private OfferStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
