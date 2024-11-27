package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invite_money")
@Getter
@Setter
public class InviteMoney {

    @Id
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TbUser tbUser;

    @Column(name = "money_sum")
    private Double moneySum;

    @Column(name = "money")
    private Double money;

    @Column(name = "cash_out")
    private Double cashOut;
}