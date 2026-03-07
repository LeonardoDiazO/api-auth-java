
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;
    private String appName;
    private String description;
}
