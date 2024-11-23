package com.rsmanager.dto.application;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class ApplicationCreateRequestDTO {
    private String fullname;
    private Integer roleId;
    private String projectName;
    private Double projectAmount;
    private String inviterName;
    private String managerName;
    private String rateA;
    private String rateB;
    private String paymentMethod;
    private Double paymentAmount;
    private Double fee;
    private LocalDate paymentTime;
    private String regionName;
    private String currency;
    private String comments;
}
