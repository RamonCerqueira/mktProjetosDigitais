package com.mktplace.service;

import com.mktplace.dto.AuthDtos.ProjectRequest;
import com.mktplace.dto.AuthDtos.ProjectResponse;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.events.ProjectLifecycleEvent;
import com.mktplace.exception.BusinessException;
import com.mktplace.messaging.MarketplaceEventPublisher;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mktplace.validation.InputSanitizer.clean;
import static com.mktplace.validation.InputSanitizer.search;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SubscriptionService subscriptionService;
    private final AuditService auditService;
    private final FraudPreventionService fraudPreventionService;
    private final ProjectIntelligenceService projectIntelligenceService;
    private final DeveloperQualificationService developerQualificationService;
    private final MarketplaceEventPublisher eventPublisher;

    public ProjectService(ProjectRepository projectRepository, SubscriptionService subscriptionService, AuditService auditService, FraudPreventionService fraudPreventionService, ProjectIntelligenceService projectIntelligenceService, DeveloperQualificationService developerQualificationService, MarketplaceEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.subscriptionService = subscriptionService;
        this.auditService = auditService;
        this.fraudPreventionService = fraudPreventionService;
        this.projectIntelligenceService = projectIntelligenceService;
        this.developerQualificationService = developerQualificationService;
        this.eventPublisher = eventPublisher;
    }

    @CacheEvict(cacheNames = {"publicProjects", "topRankedProjects"}, allEntries = true)
    public ProjectResponse create(User user, ProjectRequest request) {
        assertIndividualSeller(user);
        subscriptionService.assertCanPublish(user);
        Project project = Project.builder()
                .seller(user)
                .title(clean(request.title()))
                .description(clean(request.description()))
                .category(clean(request.category()))
                .techStack(clean(request.techStack()))
                .price(request.price())
                .monthlyRevenue(request.monthlyRevenue())
                .activeUsers(request.activeUsers())
                .status(ProjectStatus.PUBLISHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        fraudPreventionService.validateProjectListing(user, project, false);
        project = projectRepository.save(project);
        auditService.logAction("PROJECT_CREATED", "PROJECT", String.valueOf(project.getId()), project.getTitle());
        eventPublisher.publish(new ProjectLifecycleEvent(project.getId(), user.getId(), "CREATED", project.getTitle()), "integration");
        return toResponse(project, null);
    }

    @Cacheable(cacheNames = "publicProjects", key = "T(String).valueOf(#rawSearch).concat('|').concat(T(String).valueOf(#rawCity)).concat('|').concat(T(String).valueOf(#rawState))")
    public List<ProjectResponse> publicList(String rawSearch, String rawCity, String rawState) {
        String searchTerm = search(rawSearch);
        String city = clean(rawCity);
        String state = clean(rawState);
        List<Project> ranked = projectIntelligenceService.rank(projectRepository.searchPublicProjects(searchTerm, city, state).stream().filter(project -> subscriptionService.canPublish(project.getSeller())).toList());
        List<ProjectResponse> response = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) response.add(toResponse(ranked.get(i), i + 1));
        return response;
    }

    @Cacheable(cacheNames = "topRankedProjects", key = "'top10'")
    public List<ProjectResponse> topRanked() {
        return publicList(null, null, null).stream().limit(10).toList();
    }

    public List<ProjectResponse> myProjects(User user) {
        return projectRepository.findBySeller(user).stream().map(project -> toResponse(project, null)).toList();
    }

    @CacheEvict(cacheNames = {"publicProjects", "topRankedProjects"}, allEntries = true)
    public ProjectResponse update(User user, Long id, ProjectRequest request) {
        assertIndividualSeller(user);
        Project project = getOwnedProject(user, id);
        subscriptionService.assertCanPublish(user);
        project.setTitle(clean(request.title()));
        project.setDescription(clean(request.description()));
        project.setCategory(clean(request.category()));
        project.setTechStack(clean(request.techStack()));
        project.setPrice(request.price());
        project.setMonthlyRevenue(request.monthlyRevenue());
        project.setActiveUsers(request.activeUsers());
        project.setUpdatedAt(Instant.now());
        project.setStatus(ProjectStatus.PUBLISHED);
        fraudPreventionService.validateProjectListing(user, project, true);
        Project saved = projectRepository.save(project);
        auditService.logAction("PROJECT_UPDATED", "PROJECT", String.valueOf(saved.getId()), saved.getTitle());
        eventPublisher.publish(new ProjectLifecycleEvent(saved.getId(), user.getId(), "UPDATED", saved.getTitle()), "integration");
        return toResponse(saved, null);
    }

    @CacheEvict(cacheNames = {"publicProjects", "topRankedProjects"}, allEntries = true)
    public void delete(User user, Long id) {
        Project project = getOwnedProject(user, id);
        projectRepository.delete(project);
        auditService.logAction("PROJECT_DELETED", "PROJECT", String.valueOf(project.getId()), project.getTitle());
        eventPublisher.publish(new ProjectLifecycleEvent(project.getId(), user.getId(), "DELETED", project.getTitle()), "integration");
    }

    public Project getEntity(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new BusinessException("Projeto não encontrado", HttpStatus.NOT_FOUND));
    }

    @CacheEvict(cacheNames = {"publicProjects", "topRankedProjects"}, allEntries = true)
    public void markAsSold(Project project) {
        project.setStatus(ProjectStatus.SOLD);
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
        eventPublisher.publish(new ProjectLifecycleEvent(project.getId(), project.getSeller().getId(), "SOLD", project.getTitle()), "integration");
    }

    private Project getOwnedProject(User user, Long id) {
        Project project = getEntity(id);
        if (!project.getSeller().getId().equals(user.getId())) throw new BusinessException("Projeto não pertence ao usuário", HttpStatus.FORBIDDEN);
        return project;
    }

    private void assertIndividualSeller(User user) {
        if (user.getDocumentType() == null || user.getDocumentType().name().equals("CNPJ")) {
            throw new BusinessException("A plataforma é focada em dev individual: empresas com CNPJ não podem publicar projetos.", HttpStatus.BAD_REQUEST);
        }
    }

    public ProjectResponse toResponse(Project project, Integer ranking) {
        return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(), project.getCategory(), project.getTechStack(), project.getPrice(), project.getMonthlyRevenue(), project.getActiveUsers(), project.getStatus().name(), project.getSeller().getId(), project.getSeller().getName(), project.getSeller().getCity(), project.getSeller().getState(), projectIntelligenceService.score(project), projectIntelligenceService.qualification(project), ranking, projectIntelligenceService.suggestedPrice(project), projectIntelligenceService.suspicious(project), developerQualificationService.level(project.getSeller()), developerQualificationService.verified(project.getSeller()), project.isVerified());
    }
}
