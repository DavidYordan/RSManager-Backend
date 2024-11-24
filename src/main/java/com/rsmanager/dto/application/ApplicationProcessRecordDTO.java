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
public class ApplicationProcessRecordDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Long platformId;
    private Integer roleId;
    private Long inviterId;
    private String inviterName;
    private Long createrId;
    private String createrName;
    private Long teacherId;
    private String teacherName;
    private String rateA;
    private String rateB;
    private String tiktokAccount;
    private String regionName;
    private String currencyName;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private Double paidAmount;
    private Integer processStatus;
    private Instant createdAt;
    private List<ApplicationFlowRecordDTO> applicationFlowRecordDtos;
    private List<ApplicationPaymentRecordDTO> applicationPaymentRecordDtos;

    private LocalDate createdAfter;
    private LocalDate createdBefore;

    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
}

    