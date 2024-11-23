package com.rsmanager.dto.application;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentUpdateDTO {
    private Long processId;
    private Long paymentId;
    private String regionName;
    private String currency;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Double paymentAmount = 0.0;
    private Double fee = 0.0;
    private LocalDate paymentTime;
    private String comments;
    private List<String> deleteFiles;
}