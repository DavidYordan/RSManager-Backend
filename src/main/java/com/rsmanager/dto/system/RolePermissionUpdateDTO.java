package com.rsmanager.dto.system;

import lombok.Getter;

@Getter
public class RolePermissionUpdateDTO {
    private Integer roleId;
    private Integer permissionId;
    private Double rate1;
    private Double rate2;
    private Boolean isEnabled;
}
