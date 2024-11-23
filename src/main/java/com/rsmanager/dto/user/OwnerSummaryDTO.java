package com.rsmanager.dto.user;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSummaryDTO {
    private String username;
    private String regionName;
    private Double totalLearningCost;
    private Double platformTotalRevenue;
    private Double platformRevenueBalance;
    private Double platformTotalWithdrawal;
    private Double platformMoney;
    private Long inviteCount;
    private Long platformInviteCount;
    private List<CurrencyProfitData> currencyProfits;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrencyProfitData {
        private String currency;
        private List<ProfitDTO> profits;
        private List<GrowthDataDTO> growthDatas;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GrowthDataDTO {
        private String date;
        private Double profit;
        private Integer invites;
    }
}

