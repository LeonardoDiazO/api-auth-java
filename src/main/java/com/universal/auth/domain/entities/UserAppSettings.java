
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_app_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAppSettings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String settingKey;
    private String settingValue;

    @ManyToOne
    private User user;

    @ManyToOne
    private ApplicationEntity application;
}
