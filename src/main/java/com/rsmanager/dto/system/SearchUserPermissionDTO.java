package com.rsmanager.dto.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchUserPermissionDTO {
    private Long userId;
    private String username;
    private String fullname;
    private Integer roleId;
    private Integer permissionId;
    private Boolean isEnabled;
    private Boolean isCurrent;

    private Integer page = 0;
    private Integer size = 100;
}
