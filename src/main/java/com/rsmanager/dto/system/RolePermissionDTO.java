package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionDTO {
    private Integer roleId;
    private String roleName;
    private Integer permissionId;
    private String permissionName;
    private Double rate1;
    private Double rate2;
    private Boolean isEnabled;
}