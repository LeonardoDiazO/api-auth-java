
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rolepermissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
