package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usd_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsdRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "currency", nullable = false, length = 20)
    private String currency;

    @Column(name = "rate", nullable = false)
    private Double rate;

    @Column(name = "date", nullable = false)
    private LocalDate date;
}