package com.rsmanager.model;

import jakarta.persistence.*;
import java.io.Serializable;

import lombok.*;

@Entity
@Table(name = "role_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    @Column(name = "rate1", nullable = false)
    @Builder.Default
    private Double rate1 = 0.0;

    @Column(name = "rate2", nullable = false)
    @Builder.Default
    private Double rate2 = 0.0;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    public static class RolePermissionId implements Serializable {

        @Column(name = "role_id", nullable = false)
        private Integer roleId;

        @Column(name = "permission_id", nullable = false)
        private Integer permissionId;
    }
}
