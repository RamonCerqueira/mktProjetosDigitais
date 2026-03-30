package com.mktplace.repository;

import com.mktplace.model.ProjectAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectAssetRepository extends JpaRepository<ProjectAsset, Long> {
    List<ProjectAsset> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<ProjectAsset> findByIdAndProjectId(Long id, Long projectId);
}
