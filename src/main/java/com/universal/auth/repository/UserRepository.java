package com.universal.auth.repository;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndApplication_AppId(String username, Long appId);

    Optional<User> findByEmailAndApplication_AppId(String email, Long appId);

    List<User> findByApplication_AppId(Long appId);

    boolean existsByUsernameAndApplication_AppId(String username, Long appId);

    boolean existsByEmailAndApplication_AppId(String email, Long appId);
}
