package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "agent_widthdraw") // 对应表名
@Getter
@Setter
public class AgentWidthdraw {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "money")
    private Double money;

    @Column(name = "refund")
    private String refund;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "update_time")
    private Instant updateTime;
}
