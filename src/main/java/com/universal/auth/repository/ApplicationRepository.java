
package com.universal.auth.repository;

import com.universal.auth.domain.entities.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> { }
