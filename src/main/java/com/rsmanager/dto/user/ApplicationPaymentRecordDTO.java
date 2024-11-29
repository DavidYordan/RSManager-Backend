package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationPaymentRecordDTO {
        private Long userId;
        private String fullname;
        private String regionName;
        private String currencyName;
        private String currencyCode;
        private Double currencyRate;
        private String projectName;
        private Double projectAmount;
        private String projectCurrencyName;
        private String projectCurrencyCode;
        private Double projectCurrencyRate;
        private String paymentMethod;
        private Double paymentAmount;
        private Double fee;
        private Double actual;
        private LocalDate paymentDate;
        private String inviterFullname;
}
