package com.rsmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tiktok_relationship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokRelationshipRemoteB {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "tiktok_account", nullable = false)
    private String tiktokAccount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
