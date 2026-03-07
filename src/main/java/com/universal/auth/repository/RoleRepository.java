package com.universal.auth.repository;

import com.universal.auth.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByApplication_AppId(Long appId);
}
