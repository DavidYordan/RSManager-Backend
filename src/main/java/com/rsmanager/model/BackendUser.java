package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "currency_name", length = 50)
    private String currencyName;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private ApplicationProcessRecord applicationProcessRecordAsUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private TbUser tbUser;

    @OneToMany(mappedBy = "inviter")
    private List<ApplicationProcessRecord> applicationProcessRecordAsInviters;

    @OneToMany(mappedBy = "manager")
    private List<ApplicationProcessRecord> applicationProcessRecordAsManagers;

    @OneToMany(mappedBy = "creater")
    private List<ApplicationProcessRecord> applicationProcessRecordAsCreaters;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RolePermissionRelationship> rolePermissionRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TiktokRelationship> tiktokRelationships = new ArrayList<>();

    // 作为下级
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CreaterRelationship> createrRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InviterRelationship> inviterRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ManagerRelationship> managerRelationships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeacherRelationship> teacherRelationships = new ArrayList<>();

    // 作为上级
    @OneToMany(mappedBy = "creater", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CreaterRelationship> createrRelationshipAsCreaters = new ArrayList<>();

    @OneToMany(mappedBy = "inviter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InviterRelationship> inviterRelationshipAsInviters = new ArrayList<>();

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ManagerRelationship> managerRelationshipAsManagers = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeacherRelationship> teacherRelationshipAsTeachers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
