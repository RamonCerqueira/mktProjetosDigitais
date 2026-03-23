package com.mktplace.service;

import com.mktplace.model.Project;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProjectIntelligenceService {
    public int score(Project project) {
        int score = 40;
        if (project.getMonthlyRevenue() != null) {
            if (project.getMonthlyRevenue().compareTo(new BigDecimal("1000")) >= 0) score += 20;
            else if (project.getMonthlyRevenue().compareTo(BigDecimal.ZERO) > 0) score += 10;
        }
        if (project.getPrice() != null && project.getMonthlyRevenue() != null && project.getMonthlyRevenue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal multiple = project.getPrice().divide(project.getMonthlyRevenue(), 2, RoundingMode.HALF_UP);
            if (multiple.compareTo(new BigDecimal("18")) <= 0) score += 20;
            else if (multiple.compareTo(new BigDecimal("30")) <= 0) score += 10;
        }
        if (project.getTechStack() != null && !project.getTechStack().isBlank()) score += 10;
        if (project.getDescription() != null && project.getDescription().length() > 80) score += 10;
        return Math.min(score, 100);
    }

    public BigDecimal suggestedPrice(Project project) {
        if (project.getMonthlyRevenue() == null || project.getMonthlyRevenue().compareTo(BigDecimal.ZERO) <= 0) {
            return project.getPrice();
        }
        return project.getMonthlyRevenue().multiply(new BigDecimal("24")).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean suspicious(Project project) {
        if (project.getPrice() == null || project.getMonthlyRevenue() == null || project.getMonthlyRevenue().compareTo(BigDecimal.ZERO) <= 0) return false;
        BigDecimal suggested = suggestedPrice(project);
        return project.getPrice().compareTo(suggested.multiply(new BigDecimal("2.5"))) > 0 || project.getPrice().compareTo(suggested.multiply(new BigDecimal("0.30"))) < 0;
    }

    public List<Project> rank(List<Project> projects) {
        return projects.stream().sorted((a, b) -> Integer.compare(score(b), score(a))).toList();
    }
}
