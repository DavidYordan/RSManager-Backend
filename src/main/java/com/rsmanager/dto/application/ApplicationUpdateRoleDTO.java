package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationUpdateRoleDTO {
    private Long processId;
    private Integer oldStatus;
    private Integer roleId;
    private String projectName;
    private Double projectAmount;
    private String rateA;
    private String rateB;
    private String paymentMethod;
    private LocalDate startDate;
    private String comments;
    private String actionStr;
}
