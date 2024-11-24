package com.rsmanager.dto.traffic;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchTrafficDTO {
    private Long userId;
    private String username;
    private String fullname;
    private Integer roleId;
    private Long createrId;
    private String createrName;
    private String createrFullname;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Boolean inviterExists;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long teacherId;
    private String teacherName;
    private String teacherFullname;
    private Long platformId;
    private String tiktokAccount;
    private String inviterCode;
    private String invitationCode;
    private Integer invitationType;
    private String regionName;
    private String currencyName;
    private Boolean status;

    private LocalDate createdAfter;
    private LocalDate createdBefore;

    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 10;
    
}
