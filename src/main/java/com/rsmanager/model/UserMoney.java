package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_money")
@Getter
@Setter
public class UserMoney {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "money")
    private Double money;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TbUser tbUser;
}
