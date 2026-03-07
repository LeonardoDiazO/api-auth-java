package com.universal.auth.repository;

import com.universal.auth.domain.entities.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByResetTokenAndIsUsedFalse(String token);

    @Modifying
    @Query("UPDATE PasswordReset p SET p.isUsed = true " +
           "WHERE p.user.userId = :userId AND p.isUsed = false")
    void invalidatePreviousTokens(@Param("userId") Long userId);
}
