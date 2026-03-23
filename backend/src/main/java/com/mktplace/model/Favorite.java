package com.mktplace.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"}))
public class Favorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User user;
    @ManyToOne(optional = false) private Project project;
    private Instant createdAt;
}
