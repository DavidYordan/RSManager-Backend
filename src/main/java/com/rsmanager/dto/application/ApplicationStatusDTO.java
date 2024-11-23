package com.rsmanager.dto.application;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusDTO {
    private Long id;
    private Integer statusValue;
    private String statusName;
}
