package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAddDTO {
    private Long processId;
    private String regionName;
    private String currency;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Double paymentAmount;
    private Double fee;
    private LocalDate paymentTime;
    private String comments;
}