package com.rsmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_user")  // 对应远程数据库的表名
@Getter
@Setter
public class TbUser {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(mappedBy = "tbUser")
    private ApplicationProcessRecord applicationProcessRecord;

    @OneToOne(mappedBy = "tbUser")
    private BackendUser backendUser;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "open_id")
    private String openId;

    @Column(name = "wx_open_id")
    private String wxOpenId;

    @Column(name = "password")
    private String password;

    @Column(name = "create_time")
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;

    @Column(name = "apple_id")
    private String appleId;

    @Column(name = "sys_phone")
    private Integer sysPhone;

    @Column(name = "status")
    private Integer status;

    @Column(name = "platform")
    private String platform;

    @Column(name = "jifen")
    private Integer jifen;

    @Column(name = "invitation_code")
    private String invitationCode;

    @Column(name = "invitation_type")
    private Integer invitationType;

    @Column(name = "inviter_code")
    private String inviterCode;

    @Column(name = "inviter_type")
    private Integer inviterType;

    @Column(name = "inviter_custom_id")
    private Long inviterCustomId;

    @Column(name = "inviter_url")
    private String inviterUrl;

    @Column(name = "clientid")
    private String clientId;

    @Column(name = "zhi_fu_bao_name")
    private String zhiFuBaoName;

    @Column(name = "zhi_fu_bao")
    private String zhiFuBao;

    @Column(name = "wx_id")
    private String wxId;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "two_rate")
    private Double twoRate;

    @Column(name = "on_line_time")
    private String onLineTime;

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

    @Column(name = "agent0_money")
    private Double agent0Money;

    @Column(name = "agent1_money")
    private Double agent1Money;

    @Column(name = "agent0_money_delete")
    private Double agent0MoneyDelete;

    @Column(name = "agent1_money_delete")
    private Double agent1MoneyDelete;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "area_code")
    private Integer areaCode;

    @Column(name = "fake")
    private Integer fake;

    @Column(name = "register_ip")
    private String registerIp;

    @Column(name = "forwarded")
    private String forwarded;
}
