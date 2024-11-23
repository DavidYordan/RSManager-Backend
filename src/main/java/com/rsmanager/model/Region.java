package com.rsmanager.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "region_name", nullable = false, unique = true, length = 50)
    private String regionName;

    @Column(name = "currency", nullable = false, length = 50)
    private String currency;

    @Column(name = "region_code")
    private Integer regionCode;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegionProject> regionProjects;
}


