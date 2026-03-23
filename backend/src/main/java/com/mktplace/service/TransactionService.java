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

    public TransactionService(ProjectService projectService, TransactionRepository transactionRepository) {
        this.projectService = projectService;
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse purchase(User buyer, Long projectId) {
        var project = projectService.getEntity(projectId);
        if (project.getStatus() != ProjectStatus.PUBLISHED) throw new BusinessException("Projeto indisponível", HttpStatus.BAD_REQUEST);
        BigDecimal fee = project.getPrice().multiply(COMMISSION_RATE);
        BigDecimal sellerNet = project.getPrice().subtract(fee);
        Transaction tx = transactionRepository.save(Transaction.builder().buyer(buyer).seller(project.getSeller()).project(project).amount(project.getPrice()).platformFee(fee).sellerNetAmount(sellerNet).status(TransactionStatus.COMPLETED).createdAt(Instant.now()).build());
        project.setStatus(ProjectStatus.SOLD);
        return new TransactionResponse(tx.getId(), projectId, tx.getAmount(), tx.getPlatformFee(), tx.getSellerNetAmount(), tx.getStatus().name());
    }
}
