package com.mktplace.model;

import com.mktplace.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_read", columnList = "user_id,readAt"),
        @Index(name = "idx_notifications_created", columnList = "createdAt")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    @Column(length = 120)
    private String eventKey;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant readAt;
}
