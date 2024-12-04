package com.rsmanager.dto.system;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRolePermissionDTO {
    private Long recordId;
    private Double rate1;
    private Double rate2;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean status;
}
