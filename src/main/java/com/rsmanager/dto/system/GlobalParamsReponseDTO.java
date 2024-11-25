package com.rsmanager.dto.system;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalParamsReponseDTO {
    List<ProjectDTO> projectDTOs;
    List<RegionCurrencyDTO> regionCurrencyDTOs;
    List<RegionProjectsDTO> regionProjectsDTOs;
}


    