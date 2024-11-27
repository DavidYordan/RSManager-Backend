package com.rsmanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_integral") // 对应表名
@Getter
@Setter
public class UserIntegral {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private TbUser tbUser;

    @Column(name = "integral_num")
    private Integer integralNum;
}
