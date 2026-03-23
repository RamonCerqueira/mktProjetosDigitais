package com.mktplace.model;

import com.mktplace.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User seller;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 4000)
    private String description;
    private String category;
    private String techStack;
    private BigDecimal price;
    private BigDecimal monthlyRevenue;
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
