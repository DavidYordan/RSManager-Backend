package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionStrDTO {
    private Long processId;
    private Integer oldStatus;
    private Integer roleId;
    private String Fullname;
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
