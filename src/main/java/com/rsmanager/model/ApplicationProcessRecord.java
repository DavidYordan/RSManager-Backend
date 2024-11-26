package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "application_process_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationProcessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "process_id")
    private Long processId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private BackendUser user;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "fullname", length = 100, nullable = false, unique = true)
    private String fullname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private TbUser tbUser;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private BackendUser inviter;

    @Column(name = "inviter_name", nullable = false, length = 100)
    private String inviterName;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private BackendUser manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creater_id")
    private BackendUser creater;

    @Column(name = "rateA")
    private String rateA;

    @Column(name = "rateB")
    private String rateB;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "tiktok_account", length = 100)
    private String tiktokAccount;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "currency_name", nullable = false, length = 50)
    private String currencyName;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Column(name = "project_amount", nullable = false)
    @Builder.Default
    private Double projectAmount = 0.0;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "process_status", nullable = false)
    private Integer processStatus;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "action_str", columnDefinition = "TEXT")
    private String actionStr;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "applicationProcessRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationFlowRecord> applicationFlowRecords;

    @OneToMany(mappedBy = "applicationProcessRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationPaymentRecord> applicationPaymentRecords;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
