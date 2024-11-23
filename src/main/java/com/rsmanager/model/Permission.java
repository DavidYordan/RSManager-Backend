package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @Column(name = "permission_id")
    private Integer permissionId;

    @Column(name = "permission_name", length = 50)
    private String permissionName;

    @Column(name = "rate1")
    private Double rate1 = 0.0;

    @Column(name = "rate2")
    private Double rate2 = 0.0;
}
