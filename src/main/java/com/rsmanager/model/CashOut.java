package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cash_out") // 对应表名
@Getter
@Setter
public class CashOut {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "create_at")
    private String createAt;

    @Column(name = "is_out")
    private Boolean isOut;

    @Column(name = "money")
    private String money;

    @Column(name = "out_at")
    private String outAt;

    @Column(name = "relation_id")
    private String relationId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "zhifubao")
    private String zhifubao;

    @Column(name = "zhifubao_name")
    private String zhifubaoName;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "state")
    private Integer state;

    @Column(name = "refund")
    private String refund;

    @Column(name = "classify")
    private Integer classify;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "bank_number")
    private String bankNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_address")
    private String bankAddress;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "type")
    private Integer type;
}
