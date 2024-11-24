package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitDTO {
    private String fullname;
    private String inviterFullname;
    private Integer roleId;
    private String regionName;
    private String currencyName;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private Double paymentAmount;
    private Double fee;
    private Double actual;
    private Double rate;
    private Double profit;
    private Double totalProjectAmount;
    private Double totalPaymentAmount;
}
