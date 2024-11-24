package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionCurrencyDTO {
    private Integer regionCode;
    private String regionName;
    private String currencyName;
    private String currencyCode;
}
