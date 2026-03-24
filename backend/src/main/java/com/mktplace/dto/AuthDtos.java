package com.mktplace.dto;

import com.mktplace.enums.DocumentType;
import com.mktplace.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AuthDtos {
    public record RegisterRequest(@NotBlank String name, @Email String email, @NotBlank String password, Role role, DocumentType documentType, @NotBlank String documentNumber, String postalCode, String street, String streetNumber, String complement, String neighborhood, String city, String state, String companyName, Double latitude, Double longitude) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
    public record RefreshRequest(String refreshToken) {}
    public record UserResponse(Long id, String name, String email, Role role, DocumentType documentType, String documentNumber, String postalCode, String street, String streetNumber, String complement, String neighborhood, String city, String state, String companyName, Double latitude, Double longitude, boolean active, boolean blocked) {}
    public record SubscriptionResponse(String status, Instant expiresAt, BigDecimal price, boolean canPublish, boolean autoRenew, String externalReference) {}
    public record SubscriptionWebhookRequest(String eventType, Long userId, String externalReference) {}
    public record ProjectRequest(String title, String description, String category, String techStack, BigDecimal price, BigDecimal monthlyRevenue, Integer activeUsers) {}
    public record ProjectResponse(Long id, String title, String description, String category, String techStack, BigDecimal price, BigDecimal monthlyRevenue, Integer activeUsers, String status, Long sellerId, String sellerName, String sellerCity, String sellerState, Integer score, String qualification, Integer ranking, BigDecimal suggestedPrice, boolean suspicious, String sellerLevel, boolean sellerVerified, boolean projectVerified) {}
    public record OfferRequest(Long projectId, BigDecimal amount) {}
    public record CounterOfferRequest(Long offerId, BigDecimal amount) {}
    public record OfferResponse(Long id, Long projectId, BigDecimal amount, String status, Long buyerId, String buyerName, Long sellerId, String sellerName, Long proposerId, Long parentOfferId, String negotiationKey) {}
    public record OfferHistoryResponse(Long id, Long offerId, Long actorId, String actionType, BigDecimal amount, String details, Instant createdAt) {}
    public record MessageRequest(Long offerId, Long receiverId, String content) {}
    public record MessageResponse(Long id, Long offerId, String negotiationKey, Long senderId, String senderName, Long receiverId, String receiverName, String content, Instant createdAt) {}
    public record TransactionResponse(Long id, Long projectId, BigDecimal amount, BigDecimal platformFee, BigDecimal sellerNetAmount, String status, String checkoutUrl, String paymentIntentId) {}
    public record TransactionWebhookRequest(String eventType, String sessionId, String paymentIntentId) {}
    public record DashboardResponse(UserResponse user, SubscriptionResponse subscription, List<ProjectResponse> myProjects, List<OfferResponse> offers) {}
}
