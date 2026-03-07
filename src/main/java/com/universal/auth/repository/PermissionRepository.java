package com.universal.auth.repository;

import com.universal.auth.domain.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByApplication_AppId(Long appId);
}
