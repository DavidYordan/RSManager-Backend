package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
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

    @Column(name = "user_id")
    private Integer userId;
}
