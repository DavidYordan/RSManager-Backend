package com.rsmanager.dto.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionDTO {
    private Integer roleId;
    private String permissionName;
    private String classify;
    private Double rate;
    private Boolean isEnabled;
}
