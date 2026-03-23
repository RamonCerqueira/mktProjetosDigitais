package com.mktplace.service;

import com.mktplace.dto.AuthDtos.TransactionResponse;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.enums.TransactionStatus;
import com.mktplace.exception.BusinessException;
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

    public TransactionService(ProjectService projectService, TransactionRepository transactionRepository, FraudPreventionService fraudPreventionService, AuditService auditService) {
        this.projectService = projectService;
        this.transactionRepository = transactionRepository;
        this.fraudPreventionService = fraudPreventionService;
        this.auditService = auditService;
    }

    public TransactionResponse purchase(User buyer, Long projectId) {
        var project = projectService.getEntity(projectId);
        if (project.getStatus() != ProjectStatus.PUBLISHED) throw new BusinessException("Projeto indisponível", HttpStatus.BAD_REQUEST);
        fraudPreventionService.validatePurchase(buyer, project);
        BigDecimal fee = project.getPrice().multiply(COMMISSION_RATE);
        BigDecimal sellerNet = project.getPrice().subtract(fee);
        Transaction tx = transactionRepository.save(Transaction.builder().buyer(buyer).seller(project.getSeller()).project(project).amount(project.getPrice()).platformFee(fee).sellerNetAmount(sellerNet).status(TransactionStatus.COMPLETED).createdAt(Instant.now()).build());
        projectService.markAsSold(project);
        auditService.logAction("PROJECT_PURCHASED", "TRANSACTION", String.valueOf(tx.getId()), "project=" + projectId);
        return new TransactionResponse(tx.getId(), projectId, tx.getAmount(), tx.getPlatformFee(), tx.getSellerNetAmount(), tx.getStatus().name());
    }
}
