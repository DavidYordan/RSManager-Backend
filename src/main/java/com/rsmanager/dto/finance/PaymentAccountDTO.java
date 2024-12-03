package com.rsmanager.dto.finance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAccountDTO {
    private Long accountId;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private String accountBank;
    private String accountHolder;
    private String accountCurrency;
    private String accountCurrencyCode;
    private String accountRegion;
    private Boolean accountStatus;
    private String accountComments;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 100;
}
