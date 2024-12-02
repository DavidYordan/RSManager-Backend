package com.rsmanager.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tiktok_relationship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiktokRelationshipRemote {

    @Id
    @Column(name = "record_id")
    private Long recordId;

    // @Column(name = "user_id", nullable = false)
    // private Long userId;

    @Column(name = "tiktok_id", length = 50)
    private String tiktokId;

    @Column(name = "tiktok_account", nullable = false)
    private String tiktokAccount;

    // status
    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "sync_at")
    private Instant syncAt;
}
