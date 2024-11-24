package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_integral_details") // 对应表名
@Getter
@Setter
public class UserIntegralDetailsRemote {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "content")
    private String content;

    @Column(name = "classify")
    private Integer classify;

    @Column(name = "type")
    private Integer type;

    @Column(name = "num")
    private Integer num;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "day")
    private Integer day;

    @Column(name = "language_type")
    private String languageType;
}
