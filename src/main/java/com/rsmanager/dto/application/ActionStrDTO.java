package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionStrDTO {
    private Long processId;
    private Integer oldStatus;
    private Integer roleId;
    private String fullname;
    private String projectName;
    private Double projectAmount;
    private String regionName;
    private String currencyName;
    private String rateA;
    private String rateB;
    private String paymentMethod;
    private LocalDate startDate;
    private String comments;
}
