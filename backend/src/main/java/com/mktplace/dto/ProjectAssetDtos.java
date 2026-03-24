package com.mktplace.dto;

import com.mktplace.enums.AssetType;

import java.time.Instant;
import java.util.List;

public class ProjectAssetDtos {
    public record ProjectAssetResponse(Long id, Long projectId, AssetType type, String originalFilename, String contentType, long sizeBytes, Instant createdAt, String downloadUrl) {}
    public record ProjectAssetListResponse(List<ProjectAssetResponse> items) {}
}
