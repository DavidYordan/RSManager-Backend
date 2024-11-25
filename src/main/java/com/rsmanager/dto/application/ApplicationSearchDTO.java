package com.rsmanager.dto.application;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSearchDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Integer roleId;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Long platformId;
    private String invitationCode;
    private String inviterCode;
    private String rateA;
    private String rateB;
    private String tiktokAccount;
    private String regionName;
    private String currencyName;
    private String currencyCode;
    private String projectName;
    private String paymentMethod;
    private List<Integer> processStatuses;

    private LocalDate startAfter;
    private LocalDate startBefore;

    private LocalDate createdAfter;
    private LocalDate createdBefore;

    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}

    