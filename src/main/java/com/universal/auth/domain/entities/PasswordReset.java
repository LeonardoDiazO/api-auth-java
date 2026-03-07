
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "passwordresets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resetId;
    private String resetToken;
    private LocalDateTime expiresAt;
    private Boolean isUsed = false;

    @ManyToOne
    private User user;
}
