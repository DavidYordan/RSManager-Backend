package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitDTO {
    private String userFullname;
    private Integer userRoleId;
    private String inviterFullname;
    private Integer inviterRoleId;
    private String regionName;
    private String currencyName;
    private Double currencyRate;
    private String projectName;
    private Double projectAmount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private Double paymentAmount;
    private Double fee;
    private Double actual;
    private Double rate;
    private Double profit;
}
