package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "agent_money") // 对应表名
@Getter
@Setter
public class AgentMoney {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "agent_custom_id")
    private Long agentCustomId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "money")
    private Double money;

    @Column(name = "content")
    private String content;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "buy_type")
    private Integer buyType;

    @Column(name = "state")
    private Integer state;
}
