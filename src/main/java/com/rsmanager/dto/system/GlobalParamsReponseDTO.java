package com.rsmanager.dto.system;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalParamsReponseDTO {

    List<RolePermissionDTO> rolePermissions;
    List<ProjectDTO> projects;
    List<RegionDTO> regions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolePermissionDTO {
        private Integer roleId;
        private String roleName;
        private List<PermissionDTO> permissionDTOs;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PermissionDTO {
            private Integer permissionId;
            private Double rate1;
            private Double rate2;
            private Boolean isEnabled;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectDTO {
        private Integer projectId;
        private String projectName;
        private Double projectAmount;
    }
}


    