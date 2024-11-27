package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite") // 对应表名
@Getter
@Setter
public class Invite {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "invitee_user_id")
    private Integer inviteeUserId;

    @Column(name = "state")
    private Integer state;

    @Column(name = "money")
    private Double money;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "user_type")
    private Integer userType;

    @Column(name = "create_date", insertable = false, updatable = false)
    private LocalDate createDate;
}
