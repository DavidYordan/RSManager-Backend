package com.rsmanager.dto.finance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CashOutDTO {
    private Long id;
    private String createAt;
    private String money;
    private String outAt;
    private Long platformId;
    private Long userId;
    private String username;
    private String fullname;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private String orderNumber;
    private Integer state;
    private String refund;
    private Integer classify;
    private Double rate;
    private String recipient;
    private String bankNumber;
    private String bankName;
    private String bankAddress;
    private String bankCode;
    private Integer type;
}
