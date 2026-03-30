package com.mktplace.model;

import com.mktplace.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(indexes = {
        @Index(name = "idx_project_status_title", columnList = "status,title"),
        @Index(name = "idx_project_status_category", columnList = "status,category"),
        @Index(name = "idx_project_seller_created", columnList = "seller_id,createdAt"),
        @Index(name = "idx_project_created", columnList = "createdAt")
})
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
    private Integer activeUsers;
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    @Builder.Default
    private boolean verified = false;
    private String moderationNotes;
    private Instant moderatedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
