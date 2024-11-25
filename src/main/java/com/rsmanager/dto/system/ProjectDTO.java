package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    private Integer projectId;
    private Integer roleId;
    private String projectName;
    private Double projectAmount;
}
