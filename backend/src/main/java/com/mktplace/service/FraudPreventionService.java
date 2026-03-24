package com.mktplace.service;

import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FraudPreventionService {
    private final Map<String, Integer> offerAttempts = new ConcurrentHashMap<>();
    private final ProjectIntelligenceService projectIntelligenceService;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    public FraudPreventionService(ProjectIntelligenceService projectIntelligenceService, ProjectRepository projectRepository, AuditService auditService) {
        this.projectIntelligenceService = projectIntelligenceService;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    public void validateOffer(User buyer, Project project, BigDecimal amount) {
        if (project.getSeller().getId().equals(buyer.getId())) throw new BusinessException("O seller não pode negociar o próprio projeto como comprador", HttpStatus.BAD_REQUEST);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("Valor de oferta inválido", HttpStatus.BAD_REQUEST);
        if (project.getPrice() != null && amount.compareTo(project.getPrice().multiply(new BigDecimal("0.30"))) < 0) throw new BusinessException("Oferta abaixo do limite antifraude mínimo", HttpStatus.BAD_REQUEST);
        String key = buyer.getId() + ":" + project.getId();
        int attempts = offerAttempts.merge(key, 1, Integer::sum);
        if (attempts > 5) throw new BusinessException("Muitas ofertas para o mesmo projeto em curto período", HttpStatus.TOO_MANY_REQUESTS);
    }

    public void validatePurchase(User buyer, Project project) {
        if (project.getSeller().getId().equals(buyer.getId())) throw new BusinessException("Compra antifraude bloqueada para o próprio seller", HttpStatus.BAD_REQUEST);
    }

    public void validateProjectListing(User seller, Project project, boolean updating) {
        if (seller.getDocumentType() == null || seller.getDocumentType().name().equals("CNPJ")) {
            throw new BusinessException("A plataforma é focada em dev individual: apenas pessoas físicas com CPF podem publicar projetos.", HttpStatus.BAD_REQUEST);
        }
        if (project.getTitle() != null && projectRepository.existsBySellerAndTitleIgnoreCase(seller, project.getTitle()) && !updating) {
            auditService.logAction("ADMIN_ALERT_DUPLICATE_PROJECT", "PROJECT", String.valueOf(seller.getId()), project.getTitle());
            throw new BusinessException("Projeto duplicado detectado para o mesmo seller.", HttpStatus.CONFLICT);
        }
        if (projectIntelligenceService.suspicious(project)) {
            auditService.logAction("ADMIN_ALERT_SUSPICIOUS_PROJECT", "PROJECT", String.valueOf(seller.getId()), project.getTitle());
            throw new BusinessException("Projeto bloqueado pela heurística antifraude de preço", HttpStatus.BAD_REQUEST);
        }
    }
}
