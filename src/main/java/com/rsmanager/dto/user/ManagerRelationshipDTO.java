package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerRelationshipDTO {
    private Long userId;
    private String username;
    private String fullname;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean status;
}
