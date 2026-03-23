package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.service.FavoriteService;
import com.mktplace.service.UserContextService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserContextService userContextService;
    public FavoriteController(FavoriteService favoriteService, UserContextService userContextService) { this.favoriteService = favoriteService; this.userContextService = userContextService; }
    @PostMapping("/{projectId}") public void toggle(@PathVariable Long projectId) { favoriteService.toggle(userContextService.getCurrentUser(), projectId); }
    @GetMapping public List<ProjectResponse> list() { return favoriteService.list(userContextService.getCurrentUser()); }
}
