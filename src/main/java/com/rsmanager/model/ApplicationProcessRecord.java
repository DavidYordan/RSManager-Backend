package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "fullname", length = 100, nullable = false, unique = true)
    private String fullname;

    @Column(name = "platform_id")
    private Long platformId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "inviter_id")
    private Long inviterId;

    @Column(name = "inviter_name", nullable = false, length = 100)
    private String inviterName;

    @Column(name = "inviter_fullname", length = 100)
    private String inviterFullname;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "manager_name", length = 50)
    private String managerName;

    @Column(name = "manager_fullname", length = 100)
    private String managerFullname;

    @Column(name = "creater_id", nullable = false)
    private Long createrId;

    @Column(name = "creater_name", nullable = false, length = 100)
    private String createrName;

    @Column(name = "creater_fullname", length = 100)
    private String createrFullname;

    @Column(name = "paid_str", length = 200)
    private String paidStr;

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

    @Column(name = "currency", nullable = false, length = 50)
    private String currency;

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
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "applicationProcessRecord", cascade = CascadeType.ALL)
    private List<ApplicationFlowRecord> applicationFlowRecords;

    @OneToMany(mappedBy = "applicationProcessRecord", cascade = CascadeType.ALL)
    private List<ApplicationPaymentRecord> applicationPaymentRecords;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
