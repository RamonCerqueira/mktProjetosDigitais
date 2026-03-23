package com.mktplace.repository;

import com.mktplace.model.Subscription;
import com.mktplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser(User user);
    Optional<Subscription> findByExternalReference(String externalReference);
}
