package com.mktplace.service;

import com.mktplace.model.Project;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class ProjectIntelligenceService {
    private final DeveloperQualificationService developerQualificationService;

    public ProjectIntelligenceService(DeveloperQualificationService developerQualificationService) {
        this.developerQualificationService = developerQualificationService;
    }

    public int score(Project project) {
        int score = 0;
        if (project.getDescription() != null) {
            if (project.getDescription().length() >= 240) score += 35;
            else if (project.getDescription().length() >= 120) score += 25;
            else if (project.getDescription().length() >= 60) score += 15;
            if (project.getDescription().contains(".") || project.getDescription().contains(":")) score += 10;
        }
        if (project.getTitle() != null && project.getTitle().trim().split(" ").length >= 3) score += 10;
        if (project.getTechStack() != null && !project.getTechStack().isBlank()) score += 15;
        if (project.getMonthlyRevenue() != null && project.getMonthlyRevenue().compareTo(BigDecimal.ZERO) > 0) score += 15;
        if (project.getActiveUsers() != null && project.getActiveUsers() > 0) score += 15;
        return Math.min(score, 100);
    }

    public String qualification(Project project) {
        int score = score(project);
        if (score >= 75) return "Alto";
        if (score >= 45) return "Médio";
        return "Baixo";
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
        return projects.stream()
                .sorted(Comparator
                        .comparingInt((Project project) -> score(project) + developerQualificationService.reputationScore(project.getSeller()))
                        .reversed()
                        .thenComparing(Project::isVerified, Comparator.reverseOrder()))
                .toList();
    }
}
