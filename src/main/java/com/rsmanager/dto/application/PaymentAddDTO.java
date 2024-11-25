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
    private String currencyName;
    private String currencyCode;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Double paymentAmount;
    private Double fee;
    private LocalDate paymentDate;
    private String comments;
}
