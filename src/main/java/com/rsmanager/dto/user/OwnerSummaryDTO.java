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
    private Double moneySum;
    private Double money;
    private Double cashOut;
    private Double userMoney;
    private Integer userIntegral;
    private Long platformInviteCount;
    private Integer inviteCount;
    private List<CurrencyProfitData> currencyProfits;
    private List<InviteDailyMoneySumDTO> inviteDailyMoneySumDTOs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrencyProfitData {
        private String currencyName;
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

