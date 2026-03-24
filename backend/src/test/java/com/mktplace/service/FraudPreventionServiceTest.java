package com.mktplace.service;

import com.mktplace.enums.DocumentType;
import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FraudPreventionServiceTest {
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final AuditService auditService = Mockito.mock(AuditService.class);
    private final FraudPreventionService service = new FraudPreventionService(new ProjectIntelligenceService(new DeveloperQualificationService(projectRepository)), projectRepository, auditService);

    @Test
    void shouldBlockOfferBelowFraudThreshold() {
        User buyer = User.builder().id(10L).build();
        Project project = Project.builder().id(20L).seller(User.builder().id(30L).build()).price(new BigDecimal("1000")).build();

        assertThatThrownBy(() -> service.validateOffer(buyer, project, new BigDecimal("299")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("antifraude");
    }

    @Test
    void shouldBlockSuspiciousProjectListingForCnpjSeller() {
        User seller = User.builder().id(30L).documentType(DocumentType.CNPJ).createdAt(Instant.now()).build();
        Project suspicious = Project.builder()
                .seller(seller)
                .title("Projeto XPTO")
                .price(new BigDecimal("100000"))
                .monthlyRevenue(new BigDecimal("1000"))
                .activeUsers(100)
                .description("Descrição longa o suficiente para não falhar por completude")
                .build();

        assertThatThrownBy(() -> service.validateProjectListing(seller, suspicious, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pessoas físicas");
    }

    @Test
    void shouldBlockDuplicateProjectForSameSeller() {
        User seller = User.builder().id(30L).documentType(DocumentType.CPF).createdAt(Instant.now()).build();
        Project project = Project.builder().seller(seller).title("Projeto Repetido").price(new BigDecimal("1000")).monthlyRevenue(new BigDecimal("100")).activeUsers(10).description("Descrição longa o bastante para qualificação básica do projeto.").build();
        Mockito.when(projectRepository.existsBySellerAndTitleIgnoreCase(seller, "Projeto Repetido")).thenReturn(true);

        assertThatThrownBy(() -> service.validateProjectListing(seller, project, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void shouldAllowValidPurchaseForDifferentBuyerAndSeller() {
        User buyer = User.builder().id(10L).build();
        Project project = Project.builder().seller(User.builder().id(30L).build()).build();

        assertThatCode(() -> service.validatePurchase(buyer, project)).doesNotThrowAnyException();
    }
}
