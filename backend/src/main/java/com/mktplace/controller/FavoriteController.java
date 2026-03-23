package com.mktplace.controller;

import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.service.FavoriteService;
import com.mktplace.service.UserContextService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserContextService userContextService;
    public FavoriteController(FavoriteService favoriteService, UserContextService userContextService) { this.favoriteService = favoriteService; this.userContextService = userContextService; }
    @PostMapping("/{projectId}") public void toggle(@PathVariable Long projectId) { favoriteService.toggle(userContextService.getCurrentUser(), projectId); }
    @GetMapping public List<ProjectResponse> list() { return favoriteService.list(userContextService.getCurrentUser()); }
}
