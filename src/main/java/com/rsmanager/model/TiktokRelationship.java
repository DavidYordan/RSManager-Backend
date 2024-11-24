package com.rsmanager.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tiktok_relationship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private BackendUser user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiktok_id")
    private TiktokUserDetails tiktokUserDetails;

    @Column(name = "tiktok_account", nullable = false, length = 100)
    private String tiktokAccount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "creater_id", nullable = false)
    private Long createrId;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }
}
