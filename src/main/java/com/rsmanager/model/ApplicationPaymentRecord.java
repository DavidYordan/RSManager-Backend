package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_payment_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationPaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private ApplicationProcessRecord applicationProcessRecord;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "currency", nullable = false, length = 50)
    private String currency;

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Column(name = "project_amount", nullable = false)
    private Double projectAmount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "payment_amount", nullable = false)
    @Builder.Default
    private Double paymentAmount = 0.0;

    @Column(name = "fee", nullable = false)
    @Builder.Default
    private Double fee = 0.0;

    @Column(name = "actual", nullable = false)
    @Builder.Default
    private Double actual = 0.0;

    @Column(name = "payment_time", nullable = false)
    private LocalDate paymentTime;

    @Column(name = "creater_id", nullable = false)
    private Long createrId;

    @Column(name = "creater_name", nullable = false, length = 100)
    private String createrName;

    @Column(name = "creater_fullname", length = 100)
    private String createrFullname;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "finance_id")
    private Long financeId;

    @Column(name = "finance_name", length = 100)
    private String financeName;

    @Column(name = "finance_fullname", length = 100)
    private String financeFullname;
    
    @Column(name = "finance_approval_time")
    private LocalDateTime financeApprovalTime;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
