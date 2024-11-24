package com.rsmanager.dto.application;

import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewApplicationResponseDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Long platformId;
    private Integer roleId;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long teacherId;
    private String teacherName;
    private String teacherFullname;
    private String rateA;
    private String rateB;
    private LocalDate startDate;
    private String tiktokAccount;
    private String regionName;
    private String currencyName;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private String paidStr;
    private Integer processStatus;
    private Instant createdAt;
}

