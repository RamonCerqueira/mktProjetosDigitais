package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.ProjectRequest;
import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.dto.ProjectAssetDtos.ProjectAssetListResponse;
import com.mktplace.dto.ProjectAssetDtos.ProjectAssetResponse;
import com.mktplace.enums.AssetType;
import com.mktplace.service.ProjectAssetService;
import com.mktplace.service.ProjectService;
import com.mktplace.service.UserContextService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectAssetService projectAssetService;
    private final UserContextService userContextService;
    public ProjectController(ProjectService projectService, ProjectAssetService projectAssetService, UserContextService userContextService) { this.projectService = projectService; this.projectAssetService = projectAssetService; this.userContextService = userContextService; }

    @GetMapping("/marketplace/projects")
    public List<ProjectResponse> listPublic(@RequestParam(required = false) String search, @RequestParam(required = false) String city, @RequestParam(required = false) String state) { return projectService.publicList(search, city, state); }

    @GetMapping("/marketplace/projects/ranking")
    public List<ProjectResponse> ranking() { return projectService.topRanked(); }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/projects/me")
    public List<ProjectResponse> myProjects() { return projectService.myProjects(userContextService.getCurrentUser()); }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/projects")
    public ProjectResponse create(@RequestBody ProjectRequest request) { return projectService.create(userContextService.getCurrentUser(), request); }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PutMapping("/projects/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectRequest request) { return projectService.update(userContextService.getCurrentUser(), id, request); }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @DeleteMapping("/projects/{id}")
    public void delete(@PathVariable Long id) { projectService.delete(userContextService.getCurrentUser(), id); }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @PostMapping("/projects/{id}/assets")
    public ProjectAssetResponse uploadAsset(@PathVariable Long id, @RequestParam("type") AssetType type, @RequestParam("file") MultipartFile file) {
        return projectAssetService.upload(userContextService.getCurrentUser(), id, file, type);
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/projects/{id}/assets")
    public ProjectAssetListResponse listAssets(@PathVariable Long id) {
        return new ProjectAssetListResponse(projectAssetService.list(userContextService.getCurrentUser(), id));
    }

    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @GetMapping("/projects/{id}/assets/{assetId}/download")
    public ResponseEntity<Resource> downloadAsset(@PathVariable Long id, @PathVariable Long assetId) {
        return projectAssetService.download(userContextService.getCurrentUser(), id, assetId);
    }
}
