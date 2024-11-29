package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionProjectsDTO {
    private Integer regionCode;
    private String regionName;
    private String currencyCode;
    private String currencyName;
    private Integer roleId;
    private Integer projectId;
    private String projectName;
    private Double projectAmount;
}
