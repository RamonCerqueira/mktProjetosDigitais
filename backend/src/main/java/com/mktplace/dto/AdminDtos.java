package com.mktplace.dto;

import com.mktplace.enums.Role;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AdminDtos {
    public record AdminOverviewResponse(FinancialSummary financial, ConversionSummary conversion, ProjectSummary projects, List<TimeSeriesPoint> newUsersByDay, List<TimeSeriesPoint> projectsByDay, List<SellerRankingItem> topSellers) {}
    public record FinancialSummary(BigDecimal monthlyRecurringRevenue, BigDecimal totalRevenue, BigDecimal totalCommission, long activeSubscriptions, double churnRate) {}
    public record ConversionSummary(long visitors, long users, long subscribers, double visitorToUserRate, double userToSubscriberRate, double retentionRate) {}
    public record ProjectSummary(long totalProjects, long soldProjects, long suspiciousProjects) {}
    public record TimeSeriesPoint(LocalDate label, long value) {}
    public record SellerRankingItem(Long sellerId, String sellerName, long soldProjects, BigDecimal grossRevenue) {}
    public record AdminUserSummary(Long id, String name, String email, Role role, Set<Role> roles, boolean blocked, boolean active, String subscriptionStatus, Instant createdAt, Instant lastLoginAt, String city, String state) {}
    public record AdminUserDetail(AdminUserSummary user, String documentType, String documentNumber, String postalCode, String street, String streetNumber, String complement, String neighborhood, String companyName, List<AuditEntry> history) {}
    public record AuditEntry(Long id, String actorEmail, String action, String resourceType, String resourceId, String httpMethod, String path, String ipAddress, String metadata, Instant createdAt) {}
    public record UpdateRolesRequest(Set<Role> roles) {}
    public record ProjectModerationRequest(boolean verified, String status, String moderationNotes) {}
    public record AdminProjectResponse(Long id, String title, String status, boolean verified, boolean suspicious, String moderationNotes, String sellerName, BigDecimal price, BigDecimal monthlyRevenue, Instant createdAt) {}
    public record AdminTransactionResponse(Long id, Long projectId, String projectTitle, String buyerName, String sellerName, BigDecimal amount, BigDecimal platformFee, BigDecimal sellerNetAmount, String status, Instant createdAt, String paymentIntentId) {}
}
