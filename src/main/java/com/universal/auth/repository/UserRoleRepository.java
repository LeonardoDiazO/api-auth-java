
package com.universal.auth.repository;

import com.universal.auth.domain.entities.User;
import com.universal.auth.domain.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);

    boolean existsByUserAndRoleAndApplication(User user, com.universal.auth.domain.entities.Role role,
            com.universal.auth.domain.entities.ApplicationEntity application);

    void deleteByUserAndRoleAndApplication(User user, com.universal.auth.domain.entities.Role role,
            com.universal.auth.domain.entities.ApplicationEntity application);
}
