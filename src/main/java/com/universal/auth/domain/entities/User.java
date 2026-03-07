package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    // Nullable: social-only users (Google) have no password
    @Column
    private String passwordHash;

    private String fullName;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime lastPasswordChange;
    private Integer failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
    private Boolean isEmailVerified = false;

    // Every user belongs to exactly one application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private ApplicationEntity application;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRole> userRoles = new ArrayList<>();
}
