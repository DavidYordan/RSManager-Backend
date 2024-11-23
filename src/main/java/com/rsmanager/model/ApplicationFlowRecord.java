package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_flow_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationFlowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flow_id")
    private Long flowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ApplicationProcessRecord applicationProcessRecord;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "creater_id", nullable = false)
    private Long createrId;

    @Column(name = "creater_name", nullable = false, length = 100)
    private String createrName;

    @Column(name = "creater_fullname", nullable = false, length = 100)
    private String createrFullname;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "comments")
    private String comments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
