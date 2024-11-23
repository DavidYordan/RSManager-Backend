package com.rsmanager.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "backend_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendRole {

    @Id
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "backendRole", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<RolePermission> rolePermissions;

    @OneToMany(mappedBy = "role")
    private List<RoleRelationship> roleRelationships;
}
