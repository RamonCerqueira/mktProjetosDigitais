package com.mktplace.repository;

import com.mktplace.model.Project;
import com.mktplace.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = "seller")
    List<Project> findBySellerOrderByCreatedAtDesc(User seller);
    boolean existsBySellerAndTitleIgnoreCase(User seller, String title);


    @EntityGraph(attributePaths = "seller")
    @Query("""
            select p from Project p
            join p.seller s
            where p.status = com.mktplace.enums.ProjectStatus.PUBLISHED
              and (:search is null or lower(p.title) like lower(concat('%', :search, '%')))
              and (:city is null or lower(s.city) = lower(:city))
              and (:state is null or lower(s.state) = lower(:state))
            order by p.createdAt desc
            """)
    List<Project> searchPublicProjects(@Param("search") String search, @Param("city") String city, @Param("state") String state);
}
