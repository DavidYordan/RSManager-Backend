package com.rsmanager.dto.user;

import java.util.List;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitedSummaryDTO {
    private String username;
    private String regionName;
    private String currencyName;
    private List<PaymentSummaryDTO> paymentSummary;
    private List<InvitedSummaryDTO> subordinates;
}
