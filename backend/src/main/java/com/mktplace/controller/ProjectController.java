package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.ProjectRequest;
import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.service.ProjectService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProjectController {
    private final ProjectService projectService;
    private final UserContextService userContextService;
    public ProjectController(ProjectService projectService, UserContextService userContextService) { this.projectService = projectService; this.userContextService = userContextService; }

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
}
