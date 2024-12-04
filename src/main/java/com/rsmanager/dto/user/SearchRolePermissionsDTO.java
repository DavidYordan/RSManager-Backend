package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRolePermissionsDTO {
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
    private Integer permissionId;
    private Boolean status;
    private Boolean isCurrent;
    private String invitationCode;
    private String inviterCode;

    private LocalDate startDateAfter;
    private LocalDate startDateBefore;

    private LocalDate endDateAfter;
    private LocalDate endDateBefore;

    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 20;
}
