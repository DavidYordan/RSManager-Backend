package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_money_details") // 对应表名
@Getter
@Setter
public class UserMoneyDetailsRemote {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "by_user_id")
    private Integer byUserId;

    @Column(name = "title")
    private String title;

    @Column(name = "classify")
    private Integer classify;

    @Column(name = "type")
    private Integer type;

    @Column(name = "state")
    private Integer state;

    @Column(name = "state_delete")
    private Integer stateDelete;

    @Column(name = "money")
    private Double money;

    @Column(name = "content")
    private String content;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "language_type")
    private String languageType;

    @Column(name = "buy_type")
    private Integer buyType;
}
