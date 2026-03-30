package com.mktplace.service;

import com.mktplace.dto.AdminDtos.*;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.enums.Role;
import com.mktplace.enums.SubscriptionStatus;
import com.mktplace.enums.TransactionStatus;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogRepository auditLogRepository;
    private final ProjectIntelligenceService projectIntelligenceService;
    private final AuditService auditService;

    public AdminService(UserRepository userRepository, SubscriptionRepository subscriptionRepository, TransactionRepository transactionRepository, ProjectRepository projectRepository, AuditLogRepository auditLogRepository, ProjectIntelligenceService projectIntelligenceService, AuditService auditService) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.transactionRepository = transactionRepository;
        this.projectRepository = projectRepository;
        this.auditLogRepository = auditLogRepository;
        this.projectIntelligenceService = projectIntelligenceService;
        this.auditService = auditService;
    }

    public AdminOverviewResponse overview() {
        var users = userRepository.findAll();
        var subscriptions = subscriptionRepository.findAll();
        var transactions = transactionRepository.findAll();
        var projects = projectRepository.findAll();
        var logs = auditLogRepository.findAll();
        Instant monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        BigDecimal mrr = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .map(subscription -> Optional.ofNullable(subscription.getPrice()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = transactions.stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.HELD || tx.getStatus() == TransactionStatus.RELEASED)
                .map(tx -> Optional.ofNullable(tx.getAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(mrr);
        BigDecimal totalCommission = transactions.stream().map(tx -> Optional.ofNullable(tx.getPlatformFee()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeSubscriptions = subscriptions.stream().filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE).count();
        long canceledSubscriptions = subscriptions.stream().filter(subscription -> subscription.getStatus() == SubscriptionStatus.CANCELED).count();
        double churnRate = subscriptions.isEmpty() ? 0D : roundPercentage((double) canceledSubscriptions / subscriptions.size());

        long visitors = logs.stream().filter(log -> log.getPath() != null && log.getPath().startsWith("/api/marketplace")).count();
        long subscribers = activeSubscriptions;
        double visitorToUser = visitors == 0 ? 0D : roundPercentage((double) users.size() / visitors);
        double userToSubscriber = users.isEmpty() ? 0D : roundPercentage((double) subscribers / users.size());
        long retainedUsers = users.stream().filter(user -> user.isActive() && !user.isBlocked()).count();
        double retentionRate = users.isEmpty() ? 0D : roundPercentage((double) retainedUsers / users.size());

        var newUsersByDay = timeSeries(users.stream().map(User::getCreatedAt).filter(Objects::nonNull).toList());
        var projectsByDay = timeSeries(projects.stream().map(Project::getCreatedAt).filter(Objects::nonNull).toList());
        long soldProjects = projects.stream().filter(project -> project.getStatus() == ProjectStatus.SOLD).count();
        long suspiciousProjects = projects.stream().filter(projectIntelligenceService::suspicious).count();

        Map<Long, List<Project>> soldBySeller = projects.stream().filter(project -> project.getStatus() == ProjectStatus.SOLD).collect(Collectors.groupingBy(project -> project.getSeller().getId()));
        Map<Long, BigDecimal> revenueBySeller = transactions.stream().collect(Collectors.groupingBy(tx -> tx.getSeller().getId(), Collectors.mapping(tx -> Optional.ofNullable(tx.getAmount()).orElse(BigDecimal.ZERO), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        List<SellerRankingItem> topSellers = soldBySeller.entrySet().stream()
                .map(entry -> {
                    User seller = entry.getValue().getFirst().getSeller();
                    return new SellerRankingItem(seller.getId(), seller.getName(), entry.getValue().size(), revenueBySeller.getOrDefault(seller.getId(), BigDecimal.ZERO));
                })
                .sorted(Comparator.comparing(SellerRankingItem::grossRevenue).reversed())
                .limit(10)
                .toList();

        return new AdminOverviewResponse(
                new FinancialSummary(mrr, totalRevenue, totalCommission, activeSubscriptions, churnRate),
                new ConversionSummary(visitors, users.size(), subscribers, visitorToUser, userToSubscriber, retentionRate),
                new ProjectSummary(projects.size(), soldProjects, suspiciousProjects),
                newUsersByDay,
                projectsByDay,
                topSellers
        );
    }

    public List<AdminUserSummary> users(String role, String status, String subscriptionStatus) {
        Map<Long, String> subscriptions = subscriptionRepository.findAll().stream().collect(Collectors.toMap(subscription -> subscription.getUser().getId(), subscription -> subscription.getStatus().name(), (a, b) -> a));
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(user -> role == null || user.getRole().name().equalsIgnoreCase(role))
                .filter(user -> status == null || matchesUserStatus(user, status))
                .filter(user -> subscriptionStatus == null || subscriptionStatus.equalsIgnoreCase(subscriptions.get(user.getId())))
                .map(user -> toUserSummary(user, subscriptions.get(user.getId())))
                .toList();
    }

    public AdminUserDetail userDetail(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        String subStatus = subscriptionRepository.findByUser(user).map(subscription -> subscription.getStatus().name()).orElse("NONE");
        var history = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(log -> user.getEmail().equalsIgnoreCase(log.getActorEmail()) || Objects.equals(String.valueOf(user.getId()), log.getResourceId()))
                .limit(50)
                .map(log -> new AuditEntry(log.getId(), log.getActorEmail(), log.getAction(), log.getResourceType(), log.getResourceId(), log.getHttpMethod(), log.getPath(), log.getIpAddress(), log.getMetadata(), log.getCreatedAt()))
                .toList();
        return new AdminUserDetail(toUserSummary(user, subStatus), user.getDocumentType().name(), user.getDocumentNumber(), user.getPostalCode(), user.getStreet(), user.getStreetNumber(), user.getComplement(), user.getNeighborhood(), user.getCompanyName(), history);
    }

    public AdminUserSummary blockUser(Long id) { return setBlocked(id, true); }
    public AdminUserSummary unblockUser(Long id) { return setBlocked(id, false); }

    public AdminUserSummary updateRoles(Long id, UpdateRolesRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        Set<Role> roles = request.roles() == null || request.roles().isEmpty() ? Set.of(user.getRole()) : request.roles();
        user.setRoles(new HashSet<>(roles));
        user.setRole(roles.iterator().next());
        userRepository.save(user);
        auditService.logAction("ADMIN_USER_ROLES_UPDATED", "USER", String.valueOf(user.getId()), roles.toString());
        String subStatus = subscriptionRepository.findByUser(user).map(subscription -> subscription.getStatus().name()).orElse("NONE");
        return toUserSummary(user, subStatus);
    }

    public AdminUserSummary cancelAccount(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        user.setActive(false);
        user.setBlocked(true);
        user.setCanceledAt(Instant.now());
        userRepository.save(user);
        auditService.logAction("ADMIN_USER_CANCELED", "USER", String.valueOf(user.getId()), user.getEmail());
        String subStatus = subscriptionRepository.findByUser(user).map(subscription -> subscription.getStatus().name()).orElse("NONE");
        return toUserSummary(user, subStatus);
    }

    public List<AdminProjectResponse> projects(String status) {
        return projectRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(project -> status == null || project.getStatus().name().equalsIgnoreCase(status))
                .map(this::toProjectResponse)
                .toList();
    }

    public AdminProjectResponse moderateProject(Long id, ProjectModerationRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new BusinessException("Projeto não encontrado", HttpStatus.NOT_FOUND));
        project.setVerified(request.verified());
        project.setModerationNotes(request.moderationNotes());
        project.setModeratedAt(Instant.now());
        if (request.status() != null && !request.status().isBlank()) project.setStatus(ProjectStatus.valueOf(request.status().toUpperCase()));
        projectRepository.save(project);
        auditService.logAction("ADMIN_PROJECT_MODERATED", "PROJECT", String.valueOf(project.getId()), request.status());
        return toProjectResponse(project);
    }

    public void removeSuspiciousProject(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new BusinessException("Projeto não encontrado", HttpStatus.NOT_FOUND));
        project.setStatus(ProjectStatus.HIDDEN);
        project.setModerationNotes("Ocultado por suspeita administrativa");
        project.setModeratedAt(Instant.now());
        projectRepository.save(project);
        auditService.logAction("ADMIN_PROJECT_HIDDEN", "PROJECT", String.valueOf(project.getId()), project.getTitle());
    }

    public List<AdminTransactionResponse> transactions(String status) {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(tx -> status == null || tx.getStatus().name().equalsIgnoreCase(status))
                .map(tx -> new AdminTransactionResponse(tx.getId(), tx.getProject().getId(), tx.getProject().getTitle(), tx.getBuyer().getName(), tx.getSeller().getName(), tx.getAmount(), tx.getPlatformFee(), tx.getSellerNetAmount(), tx.getStatus().name(), tx.getCreatedAt(), tx.getStripePaymentIntentId()))
                .toList();
    }

    public List<AuditEntry> auditLogs(int limit) {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().limit(limit).map(log -> new AuditEntry(log.getId(), log.getActorEmail(), log.getAction(), log.getResourceType(), log.getResourceId(), log.getHttpMethod(), log.getPath(), log.getIpAddress(), log.getMetadata(), log.getCreatedAt())).toList();
    }

    private AdminUserSummary setBlocked(Long id, boolean blocked) {
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        user.setBlocked(blocked);
        userRepository.save(user);
        auditService.logAction(blocked ? "ADMIN_USER_BLOCKED" : "ADMIN_USER_UNBLOCKED", "USER", String.valueOf(user.getId()), user.getEmail());
        String subStatus = subscriptionRepository.findByUser(user).map(subscription -> subscription.getStatus().name()).orElse("NONE");
        return toUserSummary(user, subStatus);
    }

    private boolean matchesUserStatus(User user, String status) {
        return switch (status.toUpperCase()) {
            case "BLOCKED" -> user.isBlocked();
            case "ACTIVE" -> user.isActive() && !user.isBlocked();
            case "CANCELED" -> !user.isActive();
            default -> true;
        };
    }

    private AdminUserSummary toUserSummary(User user, String subscriptionStatus) {
        return new AdminUserSummary(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getRoles(), user.isBlocked(), user.isActive(), subscriptionStatus, user.getCreatedAt(), user.getLastLoginAt(), user.getCity(), user.getState());
    }

    private AdminProjectResponse toProjectResponse(Project project) {
        return new AdminProjectResponse(project.getId(), project.getTitle(), project.getStatus().name(), project.isVerified(), projectIntelligenceService.suspicious(project), project.getModerationNotes(), project.getSeller().getName(), project.getPrice(), project.getMonthlyRevenue(), project.getCreatedAt());
    }

    private List<TimeSeriesPoint> timeSeries(List<Instant> instants) {
        LocalDate start = LocalDate.now().minusDays(6);
        Map<LocalDate, Long> grouped = instants.stream().map(instant -> instant.atZone(ZoneOffset.UTC).toLocalDate()).filter(date -> !date.isBefore(start)).collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        List<TimeSeriesPoint> points = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            points.add(new TimeSeriesPoint(day, grouped.getOrDefault(day, 0L)));
        }
        return points;
    }

    private double roundPercentage(double ratio) {
        return BigDecimal.valueOf(ratio * 100).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
