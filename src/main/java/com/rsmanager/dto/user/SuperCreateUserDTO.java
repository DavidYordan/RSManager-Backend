package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperCreateUserDTO {
    private String username;
    private String fullname;
    private Integer roleId;
    private String tiktokAccount;
    private Long inviterId;
    private Long managerId;
    private Long platformId;
    private String regionName;
    private String currencyName;
    private LocalDate startDate;
}
