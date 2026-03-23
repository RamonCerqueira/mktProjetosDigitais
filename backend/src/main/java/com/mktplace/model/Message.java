package com.mktplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Offer offer;
    @ManyToOne(optional = false) private User sender;
    @ManyToOne(optional = false) private User receiver;
    @Column(nullable = false, length = 2000)
    private String content;
    private Instant createdAt;
}
