package com.mktplace.service;

import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.model.Favorite;
import com.mktplace.model.User;
import com.mktplace.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProjectService projectService;

    public FavoriteService(FavoriteRepository favoriteRepository, ProjectService projectService) {
        this.favoriteRepository = favoriteRepository;
        this.projectService = projectService;
    }

    public void toggle(User user, Long projectId) {
        var existing = favoriteRepository.findByUserIdAndProjectId(user.getId(), projectId);
        if (existing.isPresent()) favoriteRepository.delete(existing.get());
        else favoriteRepository.save(Favorite.builder().user(user).project(projectService.getEntity(projectId)).createdAt(Instant.now()).build());
    }

    public List<ProjectResponse> list(User user) {
        return favoriteRepository.findByUserId(user.getId()).stream().map(Favorite::getProject).map(projectService::toResponse).toList();
    }
}
