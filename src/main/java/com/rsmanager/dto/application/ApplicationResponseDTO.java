package com.rsmanager.dto.application;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Long platformId;
    private String invitationCode;
    private String inviterCode;
    private Integer roleId;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private String initInviterName;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private String rateA;
    private String rateB;
    private LocalDate startDate;
    private String tiktokAccount;
    private String regionName;
    private String currencyName;
    private String currencyCode;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Integer processStatus;
    private String comments;
    private String actionStr;
    private Instant createdAt;
    @Builder.Default
    private List<ApplicationPaymentRecordDTO> applicationPaymentRecordDTOs = new ArrayList<>();
    @Builder.Default
    private List<ApplicationFlowRecordDTO> applicationFlowRecordDTOs = new ArrayList<>();

    public ApplicationResponseDTO(
        Long processId,
        Long userId,
        String username,
        String fullname,
        Long platformId,
        String invitationCode,
        String inviterCode,
        Integer roleId,
        Long inviterId,
        String inviterName,
        String inviterFullname,
        String initInviterName,
        Long managerId,
        String managerName,
        String managerFullname,
        String rateA,
        String rateB,
        LocalDate startDate,
        String tiktokAccount,
        String regionName,
        String currencyName,
        String currencyCode,
        String projectName,
        Double projectAmount,
        String paymentMethod,
        Integer processStatus,
        String comments,
        String actionStr,
        Instant createdAt
    ) {
        this.processId = processId;
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.platformId = platformId;
        this.invitationCode = invitationCode;
        this.inviterCode = inviterCode;
        this.roleId = roleId;
        this.inviterId = inviterId;
        this.inviterName = inviterName;
        this.inviterFullname = inviterFullname;
        this.initInviterName = initInviterName;
        this.managerId = managerId;
        this.managerName = managerName;
        this.managerFullname = managerFullname;
        this.rateA = rateA;
        this.rateB = rateB;
        this.startDate = startDate;
        this.tiktokAccount = tiktokAccount;
        this.regionName = regionName;
        this.currencyName = currencyName;
        this.currencyCode = currencyCode;
        this.projectName = projectName;
        this.projectAmount = projectAmount;
        this.paymentMethod = paymentMethod;
        this.processStatus = processStatus;
        this.comments = comments;
        this.actionStr = actionStr;
        this.createdAt = createdAt;
    }
}

