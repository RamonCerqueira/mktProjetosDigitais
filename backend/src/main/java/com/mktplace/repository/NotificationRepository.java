package com.mktplace.repository;

import com.mktplace.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndReadAtIsNull(Long userId);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    Optional<Notification> findByEventKeyAndUserId(String eventKey, Long userId);
    List<Notification> findByUserIdAndReadAtIsNull(Long userId);
    void deleteByCreatedAtBefore(Instant cutoff);
}
