package com.mktplace.service;

import com.mktplace.exception.BusinessException;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FraudPreventionServiceTest {
    private final FraudPreventionService service = new FraudPreventionService(new ProjectIntelligenceService());

    @Test
    void shouldBlockOfferBelowFraudThreshold() {
        User buyer = User.builder().id(10L).build();
        Project project = Project.builder().id(20L).seller(User.builder().id(30L).build()).price(new BigDecimal("1000")).build();

        assertThatThrownBy(() -> service.validateOffer(buyer, project, new BigDecimal("299")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("antifraude");
    }

    @Test
    void shouldBlockSuspiciousProjectListing() {
        Project suspicious = Project.builder()
                .seller(User.builder().id(30L).build())
                .price(new BigDecimal("100000"))
                .monthlyRevenue(new BigDecimal("1000"))
                .description("Descrição longa o suficiente para não falhar por completude")
                .build();

        assertThatThrownBy(() -> service.validateProjectListing(suspicious))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("heurística antifraude");
    }

    @Test
    void shouldAllowValidPurchaseForDifferentBuyerAndSeller() {
        User buyer = User.builder().id(10L).build();
        Project project = Project.builder().seller(User.builder().id(30L).build()).build();

        assertThatCode(() -> service.validatePurchase(buyer, project)).doesNotThrowAnyException();
    }
}
