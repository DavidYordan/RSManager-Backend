package com.rsmanager.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private BackendUser user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiktok_id")
    private TiktokUserDetails tiktokUserDetails;

    @Transient
    @Builder.Default
    private List<TiktokVideoDetails> tiktokVideoDetails = new ArrayList<>();

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

    @Column(name = "sync_at")
    private Instant syncAt;
}
