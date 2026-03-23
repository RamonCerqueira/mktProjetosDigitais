package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.ProjectRequest;
import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.service.ProjectService;
import com.mktplace.service.UserContextService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProjectController {
    private final ProjectService projectService;
    private final UserContextService userContextService;
    public ProjectController(ProjectService projectService, UserContextService userContextService) { this.projectService = projectService; this.userContextService = userContextService; }
    @GetMapping("/marketplace/projects") public List<ProjectResponse> listPublic(@RequestParam(required = false) String search) { return projectService.publicList(search); }
    @GetMapping("/projects/me") public List<ProjectResponse> myProjects() { return projectService.myProjects(userContextService.getCurrentUser()); }
    @PostMapping("/projects") public ProjectResponse create(@RequestBody ProjectRequest request) { return projectService.create(userContextService.getCurrentUser(), request); }
    @PutMapping("/projects/{id}") public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectRequest request) { return projectService.update(userContextService.getCurrentUser(), id, request); }
    @DeleteMapping("/projects/{id}") public void delete(@PathVariable Long id) { projectService.delete(userContextService.getCurrentUser(), id); }
}
