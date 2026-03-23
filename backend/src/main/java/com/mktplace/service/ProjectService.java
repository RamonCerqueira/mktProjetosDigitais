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

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SubscriptionService subscriptionService;

    public ProjectService(ProjectRepository projectRepository, SubscriptionService subscriptionService) {
        this.projectRepository = projectRepository;
        this.subscriptionService = subscriptionService;
    }

    public ProjectResponse create(User user, ProjectRequest request) {
        subscriptionService.assertCanPublish(user);
        Project project = projectRepository.save(Project.builder()
                .seller(user).title(request.title()).description(request.description()).category(request.category()).techStack(request.techStack())
                .price(request.price()).monthlyRevenue(request.monthlyRevenue()).status(ProjectStatus.PUBLISHED)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        return toResponse(project);
    }

    public List<ProjectResponse> publicList(String search) {
        List<Project> projects = (search == null || search.isBlank()) ? projectRepository.findByStatus(ProjectStatus.PUBLISHED) : projectRepository.findByStatusAndTitleContainingIgnoreCase(ProjectStatus.PUBLISHED, search);
        return projects.stream().filter(project -> subscriptionService.canPublish(project.getSeller())).map(this::toResponse).toList();
    }

    public List<ProjectResponse> myProjects(User user) {
        return projectRepository.findBySeller(user).stream().map(this::toResponse).toList();
    }

    public ProjectResponse update(User user, Long id, ProjectRequest request) {
        Project project = getOwnedProject(user, id);
        subscriptionService.assertCanPublish(user);
        project.setTitle(request.title()); project.setDescription(request.description()); project.setCategory(request.category()); project.setTechStack(request.techStack()); project.setPrice(request.price()); project.setMonthlyRevenue(request.monthlyRevenue()); project.setUpdatedAt(Instant.now()); project.setStatus(ProjectStatus.PUBLISHED);
        return toResponse(projectRepository.save(project));
    }

    public void delete(User user, Long id) { projectRepository.delete(getOwnedProject(user, id)); }

    public Project getEntity(Long id) { return projectRepository.findById(id).orElseThrow(() -> new BusinessException("Projeto não encontrado", HttpStatus.NOT_FOUND)); }

    private Project getOwnedProject(User user, Long id) {
        Project project = getEntity(id);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);
        return project;
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(), project.getCategory(), project.getTechStack(), project.getPrice(), project.getMonthlyRevenue(), project.getStatus().name(), project.getSeller().getId(), project.getSeller().getName());
    }
}
