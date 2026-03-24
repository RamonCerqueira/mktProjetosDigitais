package com.mktplace.service;

import com.mktplace.enums.ProjectStatus;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import com.mktplace.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class DeveloperQualificationService {
    private final ProjectRepository projectRepository;

    public DeveloperQualificationService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public int reputationScore(User seller) {
        var projects = projectRepository.findBySeller(seller);
        long published = projects.size();
        long sold = projects.stream().filter(project -> project.getStatus() == ProjectStatus.SOLD).count();
        long months = seller.getCreatedAt() == null ? 0 : Math.max(1, Duration.between(seller.getCreatedAt(), Instant.now()).toDays() / 30);
        int score = (int) Math.min(100, published * 8 + sold * 18 + months * 2);
        return Math.max(score, seller.getDocumentType().name().equals("CPF") ? 20 : 0);
    }

    public String level(User seller) {
        int score = reputationScore(seller);
        if (score >= 80) return "Top Seller";
        if (score >= 55) return "Avançado";
        if (score >= 30) return "Intermediário";
        return "Iniciante";
    }

    public boolean verified(User seller) {
        return seller.getDocumentType() != null && seller.getDocumentType().name().equals("CPF") && !seller.isBlocked() && seller.isActive();
    }
}
