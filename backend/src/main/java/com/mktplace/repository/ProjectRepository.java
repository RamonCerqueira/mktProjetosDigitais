package com.mktplace.repository;

import com.mktplace.enums.ProjectStatus;
import com.mktplace.model.Project;
import com.mktplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findBySeller(User seller);
    List<Project> findByStatusAndTitleContainingIgnoreCase(ProjectStatus status, String title);
}
