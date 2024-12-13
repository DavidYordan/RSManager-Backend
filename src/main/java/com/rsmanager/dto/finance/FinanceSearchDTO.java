package com.rsmanager.dto.finance;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
public class FinanceSearchDTO {
    private Long userId;
    private String username;
    private String fullname;
    private Long inviterId;
    private String inviterName;
    private String inviterFullname;
    private Long managerId;
    private String managerName;
    private String managerFullname;
    private Long platformId;
    private String orderNumber;
    private Integer state;
    private String recipient;
    private String bankNumber;
    private String bankName;

    private Long idAfter;
    private Long idBefore;

    private LocalDate createdAfter;
    private LocalDate createdBefore;

    private LocalDate outAfter;
    private LocalDate outBefore;

    private int page = 0;
    private int size = 10;
}
