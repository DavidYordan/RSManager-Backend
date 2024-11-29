package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region_currency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionCurrency {

    @Id
    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;
    
    @Column(name = "region_code", nullable = false)
    private Integer regionCode;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;
    
}
