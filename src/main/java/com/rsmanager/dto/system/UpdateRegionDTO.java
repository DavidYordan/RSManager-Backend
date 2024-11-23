package com.rsmanager.dto.system;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRegionDTO {
    private Integer regionId;
    private Integer regionCode;
    private String regionName;
    private String currency;
    private List<UpdateProjectDTO> projects;
}
