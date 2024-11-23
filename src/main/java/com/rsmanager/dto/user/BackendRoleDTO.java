package com.rsmanager.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendRoleDTO {
    private Integer roleId;
    private String name;
}
