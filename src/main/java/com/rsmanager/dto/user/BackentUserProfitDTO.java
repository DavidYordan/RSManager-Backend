package com.rsmanager.dto.user;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackentUserProfitDTO {
    private String username;
    private String regionName;
    private String currencyName;
    private List<InvitedSummaryDTO> invitedSummary;
}
