package com.mktplace.service;

import com.mktplace.enums.ProjectStatus;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectIntelligenceServiceTest {
    private final ProjectIntelligenceService service = new ProjectIntelligenceService();

    @Test
    void shouldCalculateHighScoreAndSuggestedPriceForHealthyProject() {
        Project project = project("SaaS CRM", new BigDecimal("24000"), new BigDecimal("1500"), "Java, React", "Descrição longa o suficiente para passar da regra de completude e aumentar o score.");

        assertThat(service.score(project)).isEqualTo(100);
        assertThat(service.suggestedPrice(project)).isEqualByComparingTo("36000.00");
        assertThat(service.suspicious(project)).isFalse();
    }

    @Test
    void shouldRankProjectsByScoreDescending() {
        Project premium = project("Premium", new BigDecimal("18000"), new BigDecimal("1200"), "Next.js", "Descrição longa o suficiente para gerar score alto e destacar o projeto no ranking.");
        Project weak = project("Weak", new BigDecimal("90000"), new BigDecimal("200"), "", "curta");

        List<Project> ranked = service.rank(List.of(weak, premium));

        assertThat(ranked).containsExactly(premium, weak);
    }

    private Project project(String title, BigDecimal price, BigDecimal mrr, String stack, String description) {
        return Project.builder()
                .id(1L)
                .seller(User.builder().id(2L).name("Seller").email("seller@test.com").build())
                .title(title)
                .description(description)
                .category("SaaS")
                .techStack(stack)
                .price(price)
                .monthlyRevenue(mrr)
                .status(ProjectStatus.PUBLISHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
