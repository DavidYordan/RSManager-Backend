package com.rsmanager.dto.system;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionDTO {
    private Integer regionId;
    private Integer regionCode;
    private String regionName;
    private String currency;
    private List<RegionProjectDTO> regionProjectDTOs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionProjectDTO {
        private Integer projectId;
        private String projectName;
        private Double projectAmount;
    }
}
