package com.mktplace.service;

import com.mktplace.dto.AuthDtos.TransactionResponse;
import com.mktplace.dto.AuthDtos.TransactionWebhookRequest;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.enums.TransactionStatus;
import com.mktplace.events.TransactionLifecycleEvent;
import com.mktplace.exception.BusinessException;
import com.mktplace.messaging.MarketplaceEventPublisher;
import com.mktplace.model.Transaction;
import com.mktplace.model.User;
import com.mktplace.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.10");
    private final ProjectService projectService;
    private final TransactionRepository transactionRepository;
    private final FraudPreventionService fraudPreventionService;
    private final AuditService auditService;
    private final StripeService stripeService;
    private final MarketplaceEventPublisher eventPublisher;

    public TransactionService(ProjectService projectService, TransactionRepository transactionRepository, FraudPreventionService fraudPreventionService, AuditService auditService, StripeService stripeService, MarketplaceEventPublisher eventPublisher) {
        this.projectService = projectService;
        this.transactionRepository = transactionRepository;
        this.fraudPreventionService = fraudPreventionService;
        this.auditService = auditService;
        this.stripeService = stripeService;
        this.eventPublisher = eventPublisher;
    }

    public TransactionResponse purchase(User buyer, Long projectId) {
        var project = projectService.getEntity(projectId);
        if (project.getStatus() != ProjectStatus.PUBLISHED) throw new BusinessException("Projeto indisponível", HttpStatus.BAD_REQUEST);
        fraudPreventionService.validatePurchase(buyer, project);
        BigDecimal fee = project.getPrice().multiply(COMMISSION_RATE);
        BigDecimal sellerNet = project.getPrice().subtract(fee);
        Transaction tx = transactionRepository.save(Transaction.builder()
                .buyer(buyer)
                .seller(project.getSeller())
                .project(project)
                .amount(project.getPrice())
                .platformFee(fee)
                .sellerNetAmount(sellerNet)
                .status(TransactionStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        var checkout = stripeService.createCheckoutSession(tx.getId(), tx.getAmount(), project.getTitle());
        tx.setStripeCheckoutSessionId(checkout.sessionId());
        tx.setStripePaymentIntentId(checkout.paymentIntentId());
        transactionRepository.save(tx);
        auditService.logAction("TRANSACTION_CHECKOUT_CREATED", "TRANSACTION", String.valueOf(tx.getId()), "project=" + projectId);
        eventPublisher.publish(new TransactionLifecycleEvent(tx.getId(), projectId, buyer.getId(), project.getSeller().getId(), "CHECKOUT_CREATED", tx.getAmount(), tx.getStatus().name()), "integration");
        return toResponse(tx, checkout.checkoutUrl());
    }

    public TransactionResponse confirmReceipt(User buyer, Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId).orElseThrow(() -> new BusinessException("Transação não encontrada", HttpStatus.NOT_FOUND));
        if (!tx.getBuyer().getId().equals(buyer.getId())) throw new BusinessException("Somente o comprador pode confirmar a entrega", HttpStatus.FORBIDDEN);
        if (tx.getStatus() != TransactionStatus.HELD) throw new BusinessException("Transação ainda não está em escrow", HttpStatus.BAD_REQUEST);
        tx.setStatus(TransactionStatus.RELEASED);
        tx.setBuyerConfirmedAt(Instant.now());
        tx.setEscrowReleasedAt(Instant.now());
        tx.setUpdatedAt(Instant.now());
        transactionRepository.save(tx);
        projectService.markAsSold(tx.getProject());
        auditService.logAction("TRANSACTION_RELEASED", "TRANSACTION", String.valueOf(tx.getId()), tx.getStripePaymentIntentId());
        eventPublisher.publish(new TransactionLifecycleEvent(tx.getId(), tx.getProject().getId(), tx.getBuyer().getId(), tx.getSeller().getId(), "RELEASED", tx.getAmount(), tx.getStatus().name()), "integration");
        return toResponse(tx, null);
    }

    public TransactionResponse refund(User user, Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId).orElseThrow(() -> new BusinessException("Transação não encontrada", HttpStatus.NOT_FOUND));
        if (!tx.getBuyer().getId().equals(user.getId()) && !tx.getSeller().getId().equals(user.getId())) throw new BusinessException("Sem permissão para refund", HttpStatus.FORBIDDEN);
        if (tx.getStatus() == TransactionStatus.REFUNDED) throw new BusinessException("Transação já foi reembolsada", HttpStatus.BAD_REQUEST);
        tx.setStatus(TransactionStatus.REFUNDED);
        tx.setRefundedAt(Instant.now());
        tx.setUpdatedAt(Instant.now());
        transactionRepository.save(tx);
        auditService.logAction("TRANSACTION_REFUNDED", "TRANSACTION", String.valueOf(tx.getId()), tx.getStripePaymentIntentId());
        eventPublisher.publish(new TransactionLifecycleEvent(tx.getId(), tx.getProject().getId(), tx.getBuyer().getId(), tx.getSeller().getId(), "REFUNDED", tx.getAmount(), tx.getStatus().name()), "integration");
        return toResponse(tx, null);
    }

    public void handleStripeWebhook(TransactionWebhookRequest request) {
        Transaction tx = request.sessionId() != null
                ? transactionRepository.findByStripeCheckoutSessionId(request.sessionId()).orElseThrow(() -> new BusinessException("Transação não encontrada", HttpStatus.NOT_FOUND))
                : transactionRepository.findByStripePaymentIntentId(request.paymentIntentId()).orElseThrow(() -> new BusinessException("Transação não encontrada", HttpStatus.NOT_FOUND));
        switch (request.eventType()) {
            case "checkout.session.completed", "payment_intent.succeeded" -> tx.setStatus(TransactionStatus.HELD);
            case "charge.refunded", "payment_intent.canceled" -> tx.setStatus(TransactionStatus.REFUNDED);
            default -> throw new BusinessException("Evento financeiro não suportado", HttpStatus.BAD_REQUEST);
        }
        tx.setUpdatedAt(Instant.now());
        if (tx.getStatus() == TransactionStatus.REFUNDED) tx.setRefundedAt(Instant.now());
        transactionRepository.save(tx);
        auditService.logAction("TRANSACTION_WEBHOOK_" + request.eventType(), "TRANSACTION", String.valueOf(tx.getId()), tx.getStripePaymentIntentId());
        eventPublisher.publish(new TransactionLifecycleEvent(tx.getId(), tx.getProject().getId(), tx.getBuyer().getId(), tx.getSeller().getId(), request.eventType(), tx.getAmount(), tx.getStatus().name()), "integration");
    }

    public boolean validateStripeSignature(String payload, String signature) {
        return stripeService.validateWebhook(payload, signature);
    }

    private TransactionResponse toResponse(Transaction tx, String checkoutUrl) {
        return new TransactionResponse(tx.getId(), tx.getProject().getId(), tx.getAmount(), tx.getPlatformFee(), tx.getSellerNetAmount(), tx.getStatus().name(), checkoutUrl, tx.getStripePaymentIntentId());
    }
}
