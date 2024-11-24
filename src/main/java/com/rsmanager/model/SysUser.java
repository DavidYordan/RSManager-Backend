package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_user") // 对应表名
@Getter
@Setter
public class SysUser {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "salt")
    private String salt;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_user_id")
    private Long createUserId;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "app_user_id")
    private Long appUserId;

    @Column(name = "is_agent")
    private Integer isAgent;

    @Column(name = "agent_cash")
    private Double agentCash;

    @Column(name = "agent_withdraw_cash")
    private Double agentWithdrawCash;

    @Column(name = "agent0_money")
    private Double agent0Money;

    @Column(name = "agent_rate")
    private Float agentRate;

    @Column(name = "agent_bank_user")
    private String agentBankUser;

    @Column(name = "agent_bank_account")
    private String agentBankAccount;

    @Column(name = "agent_bank_name")
    private String agentBankName;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "agent_type")
    private Integer agentType;

    @Column(name = "agent_bank_address")
    private String agentBankAddress;

    @Column(name = "agent_bank_code")
    private String agentBankCode;
}
