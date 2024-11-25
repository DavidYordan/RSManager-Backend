package com.rsmanager.dto.application;

import lombok.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationInfoResponseDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Long platformId;
    private String invitationCode;
    private Integer roleId;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private String inviterCode;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private String rateA;
    private String rateB;
    private LocalDate startDate;
    private String tiktokAccount;
    private String regionName;
    private String currencyName;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Integer processStatus;
    private Instant createdAt;
    private String comments;
    private String actionStr;
    private List<ApplicationFlowRecordDTO> applicationFlowRecordDtos;
    private List<ApplicationPaymentRecordDTO> applicationPaymentRecordDtos;
}

