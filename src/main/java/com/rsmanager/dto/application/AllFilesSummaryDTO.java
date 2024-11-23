package com.rsmanager.dto.application;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllFilesSummaryDTO {
    private List<PaymentSummaryDTO> paymentsSummaryDTOs;
    private FileSummaryDTO contractSummaryDTO;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummaryDTO {
        private Long paymentId;
        private int fileCount;
        private List<String> filePaths;
    }
}
