package com.rsmanager.dto.application;

import java.time.LocalDate;
import java.time.Instant;

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
    private String projectName;
    private Double projectAmount;
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
    private Boolean status;
}
