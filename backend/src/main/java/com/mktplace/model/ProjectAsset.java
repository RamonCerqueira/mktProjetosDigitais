package com.mktplace.model;

import com.mktplace.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_assets", indexes = {
        @Index(name = "idx_asset_project_type", columnList = "project_id,type"),
        @Index(name = "idx_asset_created", columnList = "createdAt"),
        @Index(name = "idx_asset_storage_key", columnList = "storageKey", unique = true)
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType type;

    @Column(nullable = false, length = 150)
    private String originalFilename;

    @Column(nullable = false, length = 180)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant createdAt;
}
