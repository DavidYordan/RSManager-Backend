package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

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

    @ManyToOne
    @JoinColumn(name = "process_id", nullable = false)
    private ApplicationProcessRecord applicationProcessRecord;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

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

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "creater_id", nullable = false)
    private Long createrId;

    @Column(name = "creater_name", nullable = false, length = 100)
    private String createrName;

    @Column(name = "creater_fullname", length = 100)
    private String createrFullname;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "finance_id")
    private Long financeId;

    @Column(name = "finance_name", length = 100)
    private String financeName;

    @Column(name = "finance_fullname", length = 100)
    private String financeFullname;
    
    @Column(name = "finance_approval_time")
    private Instant financeApprovalTime;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
