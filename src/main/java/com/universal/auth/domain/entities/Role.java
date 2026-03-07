
package com.universal.auth.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    private String roleName;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private ApplicationEntity application;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<RolePermission> rolePermissions = new ArrayList<>();
}
