package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationProcessUpdateDTO {
    private Long processId;
    private String fullname;
    private String inviterName;
    private String managerName;
    private Integer roleId;
    private String regionName;
    private String currencyName;
    private String projectName;
    private Double projectAmount;
    private String rateA;
    private String rateB;
    private LocalDate startDate;
    private String paymentMethod;
    private String comments;
}
