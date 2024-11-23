package com.rsmanager.model;

import jakarta.persistence.*;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    @JsonBackReference
    private BackendRole backendRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Builder.Default
    @Column(name = "rate1")
    private Double rate1 = 0.0;

    @Builder.Default
    @Column(name = "rate2")
    private Double rate2 = 0.0;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

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
