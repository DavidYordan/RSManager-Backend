package com.rsmanager.dto.application;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationUpdateDTO {
    private Long processId;
    private Long userId;
    private String username;
    private String fullname;
    private Long inviterId;
    private Long managerId;
    private LocalDate startDate;
}
