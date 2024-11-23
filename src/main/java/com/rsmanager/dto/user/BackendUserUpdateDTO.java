package com.rsmanager.dto.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendUserUpdateDTO {
    private Long userId;
    private String fullname;
    private Integer roleId;
    private Long inviterId;
    private Long managerId;
    private Long teacherId;
    private String tiktokAccount;
    private LocalDate startDate;
    private Boolean status;
    @Builder.Default
    List<PermissionUpdateDTO> PermissionUpdateDTO = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PermissionUpdateDTO {
        private Long permissionId;
        private Double rate1;
        private Double rate2;
        private Boolean status;
    }
}
