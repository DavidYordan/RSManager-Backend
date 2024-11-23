package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite_money") // 对应表名
@Getter
@Setter
public class InviteMoney {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "money_sum")
    private Double moneySum;

    @Column(name = "money")
    private Double money;

    @Column(name = "cash_out")
    private Double cashOut;
}