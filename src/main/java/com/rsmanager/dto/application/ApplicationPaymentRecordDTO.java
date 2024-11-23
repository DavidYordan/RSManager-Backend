package com.rsmanager.dto.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String currency;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Double paymentAmount;
    private Double fee;
    private Double actual;
    private LocalDate paymentTime;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private LocalDateTime createdAt;
    private Long financeId;
    private String financeName;
    private String financeFullname;
    private LocalDateTime financeApprovalTime;
    private String comments;
    private Boolean status;
}
