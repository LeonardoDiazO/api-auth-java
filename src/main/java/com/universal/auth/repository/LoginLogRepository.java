
package com.universal.auth.repository;

import com.universal.auth.domain.entities.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> { }
