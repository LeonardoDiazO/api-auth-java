
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private ApplicationEntity application;
}
