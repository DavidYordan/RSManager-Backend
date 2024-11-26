package com.rsmanager.dto.user;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionRelationshipDTO {
    private Long recordId;
    private Integer roleId;
    private String roleName;
    private Integer permissionId;
    private String permissionName;
    private Double rate1;
    private Double rate2;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean status;
}
