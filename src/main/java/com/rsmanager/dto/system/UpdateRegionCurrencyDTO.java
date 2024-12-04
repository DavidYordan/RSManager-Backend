package com.rsmanager.dto.system;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRegionCurrencyDTO {
    private String regionName;
    private Integer regionCode;
    private String currencyName;
    private String currencyCode;
}
