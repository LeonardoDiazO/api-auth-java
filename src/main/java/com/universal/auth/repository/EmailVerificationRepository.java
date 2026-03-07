package com.universal.auth.repository;

import com.universal.auth.domain.entities.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByTokenAndIsUsedFalse(String token);

    @Modifying
    @Query("UPDATE EmailVerification e SET e.isUsed = true " +
           "WHERE e.user.userId = :userId AND e.isUsed = false")
    void invalidatePreviousTokens(@Param("userId") Long userId);
}
