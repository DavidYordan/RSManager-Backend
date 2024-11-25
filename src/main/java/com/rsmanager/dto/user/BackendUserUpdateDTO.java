package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendUserUpdateDTO {
    private Long userId;
    private String fullname;
    private Long inviterId;
    private Long managerId;
    private Long teacherId;
    private String tiktokAccount;
    private LocalDate startDate;
    private Boolean status;
}
