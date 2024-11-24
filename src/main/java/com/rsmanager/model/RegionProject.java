package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionProject {

    @Id
    @Column(name = "region_code", nullable = false)
    private Integer regionCode;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "project_name", nullable = false, length = 50)
    private String projectName;

    @Column(name = "project_amount", nullable = false)
    private Double projectAmount;
}
