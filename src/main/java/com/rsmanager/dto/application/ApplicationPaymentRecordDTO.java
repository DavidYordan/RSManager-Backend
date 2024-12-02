package com.rsmanager.dto.application;

import java.time.Instant;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationPaymentRecordDTO {
    private Long paymentId;
    private Long processId;
    private String regionName;
    private String currencyName;
    private String currencyCode;
    private Double currencyRate;
    private String projectName;
    private Double projectAmount;
    private String projectCurrencyName;
    private String projectCurrencyCode;
    private Double projectCurrencyRate;
    private String paymentMethod;
    private Double paymentAmount;
    private Double fee;
    private Double actual;
    private LocalDate paymentDate;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Instant createdAt;
    private Long financeId;
    private String financeName;
    private String financeFullname;
    private Instant financeApprovalTime;
    private String comments;
    private Integer status;
    private String paymentAccountStr;
}
