package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(nullable = false, length = 255)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    private LocalDateTime createdAt;
}
