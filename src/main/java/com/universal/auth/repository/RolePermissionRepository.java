package com.universal.auth.repository;

import com.universal.auth.domain.entities.Role;
import com.universal.auth.domain.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(Role role);

    boolean existsByRoleAndPermission(Role role, com.universal.auth.domain.entities.Permission permission);

    void deleteByRoleAndPermission(Role role, com.universal.auth.domain.entities.Permission permission);
}
