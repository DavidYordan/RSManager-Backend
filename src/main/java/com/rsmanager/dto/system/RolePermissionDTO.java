package com.rsmanager.dto.system;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionDTO {
    private Integer roleId;
    private String roleName;
    private List<PermissionDTO> permissionDTOs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PermissionDTO {
        private Integer permissionId;
        private Double rate1;
        private Double rate2;
        private Boolean isEnabled;
    }
}