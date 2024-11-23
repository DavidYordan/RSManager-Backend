package com.rsmanager.dto.user;

import java.util.Date;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformInvitedSummaryDTO {
    private String username;
    private Date invitedDate;
    private Double profit;
}
