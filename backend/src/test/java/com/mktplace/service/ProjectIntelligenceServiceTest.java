package com.mktplace.service;

import com.mktplace.enums.DocumentType;
import com.mktplace.enums.ProjectStatus;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectIntelligenceServiceTest {
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final ProjectIntelligenceService service = new ProjectIntelligenceService(new DeveloperQualificationService(projectRepository));

    @Test
    void shouldCalculateHighScoreAndSuggestedPriceForHealthyProject() {
        Project project = project("SaaS CRM", new BigDecimal("24000"), new BigDecimal("1500"), 120, "Java, React", "Descrição longa o suficiente para passar da regra de completude e aumentar o score com bastante clareza do projeto.");

        assertThat(service.score(project)).isGreaterThanOrEqualTo(75);
        assertThat(service.qualification(project)).isEqualTo("Alto");
        assertThat(service.suggestedPrice(project)).isEqualByComparingTo("36000.00");
        assertThat(service.suspicious(project)).isFalse();
    }

    @Test
    void shouldRankProjectsByScoreAndSellerStrengthDescending() {
        User strongSeller = User.builder().id(2L).name("Strong").email("strong@test.com").documentType(DocumentType.CPF).createdAt(Instant.now().minusSeconds(3600L * 24 * 400)).build();
        User weakSeller = User.builder().id(3L).name("Weak").email("weak@test.com").documentType(DocumentType.CPF).createdAt(Instant.now()).build();
        Project premium = project("Premium", new BigDecimal("18000"), new BigDecimal("1200"), 50, "Next.js", "Descrição longa o suficiente para gerar score alto e destacar o projeto no ranking.");
        premium.setSeller(strongSeller);
        Project weak = project("Weak", new BigDecimal("90000"), new BigDecimal("200"), null, "", "curta");
        weak.setSeller(weakSeller);
        Mockito.when(projectRepository.findBySeller(strongSeller)).thenReturn(List.of(premium, project("Sold", new BigDecimal("1000"), new BigDecimal("200"), 10, "Java", "descricao longa suficiente para apoiar reputação")));
        Mockito.when(projectRepository.findBySeller(weakSeller)).thenReturn(List.of(weak));

        List<Project> ranked = service.rank(List.of(weak, premium));

        assertThat(ranked).containsExactly(premium, weak);
    }

    private Project project(String title, BigDecimal price, BigDecimal mrr, Integer activeUsers, String stack, String description) {
        return Project.builder()
                .id(1L)
                .seller(User.builder().id(2L).name("Seller").email("seller@test.com").documentType(DocumentType.CPF).createdAt(Instant.now()).build())
                .title(title)
                .description(description)
                .category("SaaS")
                .techStack(stack)
                .price(price)
                .monthlyRevenue(mrr)
                .activeUsers(activeUsers)
                .status(ProjectStatus.PUBLISHED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
