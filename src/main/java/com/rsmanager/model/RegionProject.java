package com.rsmanager.model;

import java.io.Serializable;

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

    @EmbeddedId
    private RegionProjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("regionId")
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "project_name", length = 50, nullable = false)
    private String projectName;

    @Column(name = "project_amount", nullable = false)
    @Builder.Default
    private Double projectAmount = 0.0;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Embeddable
    @EqualsAndHashCode
    public static class RegionProjectId implements Serializable {

        @Column(name = "region_id", nullable = false)
        private Integer regionId;

        @Column(name = "project_id", nullable = false)
        private Integer projectId;
    }
}
