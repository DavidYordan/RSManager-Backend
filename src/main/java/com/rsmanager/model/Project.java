package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    
    @Id
    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "project_name", length = 50, nullable = false)
    private String projectName;

    @Column(name = "project_amount", nullable = false)
    @Builder.Default
    private Double projectAmount = 0.0;
}
