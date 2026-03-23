package com.mktplace.service;

import com.mktplace.dto.AuthDtos.ProjectRequest;
import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.mktplace.validation.InputSanitizer.clean;
import static com.mktplace.validation.InputSanitizer.search;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;

    public ProjectService(ProjectRepository projectRepository, SubscriptionService subscriptionService, AuditService auditService) {
        this.projectRepository = projectRepository;
        this.subscriptionService = subscriptionService;
        this.auditService = auditService;
    }

    public ProjectResponse create(User user, ProjectRequest request) {
        subscriptionService.assertCanPublish(user);
        Project project = projectRepository.save(Project.builder()
                .seller(user)
                .title(clean(request.title()))
                .description(clean(request.description()))
                .category(clean(request.category()))
                .techStack(clean(request.techStack()))
                .price(request.price())
                .monthlyRevenue(request.monthlyRevenue())
                .status(ProjectStatus.PUBLISHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        auditService.logAction("PROJECT_CREATED", "PROJECT", String.valueOf(project.getId()), project.getTitle());
        return toResponse(project);
    }

    public List<ProjectResponse> publicList(String rawSearch) {
        String searchTerm = search(rawSearch);
        List<Project> projects = (searchTerm == null || searchTerm.isBlank()) ? projectRepository.findByStatus(ProjectStatus.PUBLISHED) : projectRepository.findByStatusAndTitleContainingIgnoreCase(ProjectStatus.PUBLISHED, searchTerm);
        return projects.stream().filter(project -> subscriptionService.canPublish(project.getSeller())).map(this::toResponse).toList();
    }

    public List<ProjectResponse> myProjects(User user) {
        return projectRepository.findBySeller(user).stream().map(this::toResponse).toList();
    }

    public ProjectResponse update(User user, Long id, ProjectRequest request) {
        Project project = getOwnedProject(user, id);
        subscriptionService.assertCanPublish(user);
        project.setTitle(clean(request.title()));
        project.setDescription(clean(request.description()));
        project.setCategory(clean(request.category()));
        project.setTechStack(clean(request.techStack()));
        project.setPrice(request.price());
        project.setMonthlyRevenue(request.monthlyRevenue());
        project.setUpdatedAt(Instant.now());
        project.setStatus(ProjectStatus.PUBLISHED);
        Project saved = projectRepository.save(project);
        auditService.logAction("PROJECT_UPDATED", "PROJECT", String.valueOf(saved.getId()), saved.getTitle());
        return toResponse(saved);
    }

    public void delete(User user, Long id) {
        Project project = getOwnedProject(user, id);
        projectRepository.delete(project);
        auditService.logAction("PROJECT_DELETED", "PROJECT", String.valueOf(project.getId()), project.getTitle());
    }

    public Project getEntity(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new BusinessException("Projeto não encontrado", HttpStatus.NOT_FOUND));
    }

    public void markAsSold(Project project) {
        project.setStatus(ProjectStatus.SOLD);
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
    }

    private Project getOwnedProject(User user, Long id) {
        Project project = getEntity(id);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);
        return project;
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(), project.getCategory(), project.getTechStack(), project.getPrice(), project.getMonthlyRevenue(), project.getStatus().name(), project.getSeller().getId(), project.getSeller().getName());
    }
}
