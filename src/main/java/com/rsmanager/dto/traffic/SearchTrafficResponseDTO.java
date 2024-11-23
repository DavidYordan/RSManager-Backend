package com.rsmanager.dto.traffic;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchTrafficResponseDTO {
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
    private String currency;
    private Boolean status;
}
