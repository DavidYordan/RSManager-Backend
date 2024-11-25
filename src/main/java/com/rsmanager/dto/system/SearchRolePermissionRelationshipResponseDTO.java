package com.rsmanager.dto.system;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRolePermissionRelationshipResponseDTO {
    private Long recordId;
    private Long userId;
    private String username;
    private String fullname;
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
