package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_integral") // 对应表名
@Getter
@Setter
public class UserIntegralRemote {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "integral_num")
    private Integer integralNum;
}
