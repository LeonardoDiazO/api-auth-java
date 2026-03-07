package com.universal.auth.repository;

import com.universal.auth.domain.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true " +
           "WHERE r.user.userId = :userId AND r.appId = :appId AND r.revoked = false")
    void revokeAllByUserIdAndAppId(@Param("userId") Long userId, @Param("appId") Long appId);
}
