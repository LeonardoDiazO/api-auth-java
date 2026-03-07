
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;
    private String permissionName;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private ApplicationEntity application;
}
