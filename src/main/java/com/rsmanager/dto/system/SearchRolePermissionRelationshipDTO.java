package com.rsmanager.dto.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRolePermissionRelationshipDTO {
    private Long userId;
    private String username;
    private String fullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Integer roleId;
    private String roleName;
    private Integer permissionId;
    private String permissionName;
    private Boolean isEnabled;
    private Boolean isCurrent;
    private String invitationCode;
    private String inviterCode;

    private Integer page = 0;
    private Integer size = 50;
}
