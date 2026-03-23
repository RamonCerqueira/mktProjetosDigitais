package com.mktplace.model;

import com.mktplace.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User buyer;
    @ManyToOne(optional = false) private User seller;
    @ManyToOne(optional = false) private Project project;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal sellerNetAmount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private Instant createdAt;
}
