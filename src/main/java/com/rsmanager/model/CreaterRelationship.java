package com.rsmanager.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "creater_relationship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreaterRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private BackendUser user;

    @ManyToOne
    @JoinColumn(name = "creater_id", nullable = false)
    private BackendUser creater;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
    }

}