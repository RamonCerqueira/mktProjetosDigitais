package com.mktplace.repository;

import com.mktplace.model.PasswordResetToken;
import com.mktplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);
    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);
}
