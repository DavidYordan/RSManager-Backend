package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "backend_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendUser {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "fullname", length = 100)
    private String fullname;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_learning_cost", nullable = false)
    @Builder.Default
    private Double totalLearningCost = 0.0;

    @Column(name = "total_revenue", nullable = false)
    @Builder.Default
    private Double totalRevenue = 0.0;

    @Column(name = "revenue_balance", nullable = false)
    @Builder.Default
    private Double revenueBalance = 0.0;

    @Column(name = "total_withdrawal", nullable = false)
    @Builder.Default
    private Double totalWithdrawal = 0.0;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "currency", length = 50)
    private String currency;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<RoleRelationship> roleRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<PermissionRelationship> permissionRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<TiktokRelationship> tiktokRelationships = new ArrayList<>();

    // 作为下级
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<CreaterRelationship> createrRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<InviterRelationship> inviterRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ManagerRelationship> managerRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<TeacherRelationship> teacherRelationships = new ArrayList<>();

    // 作为上级
    @OneToMany(mappedBy = "creater", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<CreaterRelationship> createrRelationships2 = new ArrayList<>();

    @OneToMany(mappedBy = "inviter", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<InviterRelationship> inviterRelationships2 = new ArrayList<>();

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ManagerRelationship> managerRelationships2 = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<TeacherRelationship> teacherRelationships2 = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RoleRelationship getRole(LocalDate date) {
        return roleRelationships.stream()
                .filter(roleRelationship -> roleRelationship.getStatus())
                .filter(roleRelationship -> roleRelationship.getStartDate().isBefore(date) || roleRelationship.getStartDate().isEqual(date))
                .filter(roleRelationship -> roleRelationship.getEndDate() == null || roleRelationship.getEndDate().isAfter(date) || roleRelationship.getEndDate().isEqual(date))
                .findFirst()
                .orElse(null);
    }

    public RoleRelationship getRole() {
        return getRole(LocalDate.now());
    }

    public List<PermissionRelationship> getPermissions(LocalDate date) {
        return permissionRelationships.stream()
                .filter(permissionRelationship -> permissionRelationship.getStatus())
                .filter(permissionRelationship -> permissionRelationship.getStartDate().isBefore(date) || permissionRelationship.getStartDate().isEqual(date))
                .filter(permissionRelationship -> permissionRelationship.getEndDate() == null || permissionRelationship.getEndDate().isAfter(date) || permissionRelationship.getEndDate().isEqual(date))
                .collect(Collectors.toList());
    }

    public List<PermissionRelationship> getPermissions() {
        return getPermissions(LocalDate.now());
    }

    public TiktokAccount getTiktokAccount(LocalDate date) {
        return tiktokRelationships.stream()
                .filter(tiktokRelationship -> tiktokRelationship.getStatus())
                .filter(tiktokRelationship -> tiktokRelationship.getStartDate().isBefore(date) || tiktokRelationship.getStartDate().isEqual(date))
                .filter(tiktokRelationship -> tiktokRelationship.getEndDate() == null || tiktokRelationship.getEndDate().isAfter(date) || tiktokRelationship.getEndDate().isEqual(date))
                .map(TiktokRelationship::getTiktoker)
                .findFirst()
                .orElse(null);
    }

    public TiktokAccount getTiktokAccount() {
        return getTiktokAccount(LocalDate.now());
    }

    public BackendUser getCreater(LocalDate date) {
        return createrRelationships.stream()
                .filter(createrRelationship -> createrRelationship.getStartDate().isBefore(date) || createrRelationship.getStartDate().isEqual(date))
                .filter(createrRelationship -> createrRelationship.getEndDate() == null || createrRelationship.getEndDate().isAfter(date) || createrRelationship.getEndDate().isEqual(date))
                .map(CreaterRelationship::getCreater)
                .findFirst()
                .orElse(null);
    }

    public BackendUser getCreaterId() {
        return getCreater(LocalDate.now());
    }

    public BackendUser getInviter(LocalDate date) {
        return inviterRelationships.stream()
                .filter(inviterRelationship -> inviterRelationship.getStatus())
                .filter(inviterRelationship -> inviterRelationship.getStartDate().isBefore(date) || inviterRelationship.getStartDate().isEqual(date))
                .filter(inviterRelationship -> inviterRelationship.getEndDate() == null || inviterRelationship.getEndDate().isAfter(date) || inviterRelationship.getEndDate().isEqual(date))
                .map(InviterRelationship::getInviter)
                .findFirst()
                .orElse(null);
    }

    public BackendUser getInviter() {
        return getInviter(LocalDate.now());
    }

    public BackendUser getManager(LocalDate date) {
        return managerRelationships.stream()
                .filter(managerRelationship -> managerRelationship.getStatus())
                .filter(managerRelationship -> managerRelationship.getStartDate().isBefore(date) || managerRelationship.getStartDate().isEqual(date))
                .filter(managerRelationship -> managerRelationship.getEndDate() == null || managerRelationship.getEndDate().isAfter(date) || managerRelationship.getEndDate().isEqual(date))
                .map(ManagerRelationship::getManager)
                .findFirst()
                .orElse(null);
    }

    public BackendUser getManager() {
        return getManager(LocalDate.now());
    }

    public BackendUser getTeacher(LocalDate date) {
        return teacherRelationships.stream()
                .filter(teacherRelationship -> teacherRelationship.getStatus())
                .filter(teacherRelationship -> teacherRelationship.getStartDate().isBefore(date) || teacherRelationship.getStartDate().isEqual(date))
                .filter(teacherRelationship -> teacherRelationship.getEndDate() == null || teacherRelationship.getEndDate().isAfter(date) || teacherRelationship.getEndDate().isEqual(date))
                .map(TeacherRelationship::getTeacher)
                .findFirst()
                .orElse(null);
    }

    public BackendUser getTeacher() {
        return getTeacher(LocalDate.now());
    }
}
