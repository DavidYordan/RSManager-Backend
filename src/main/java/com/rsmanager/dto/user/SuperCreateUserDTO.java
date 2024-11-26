package com.rsmanager.dto.user;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperCreateUserDTO {
    private Long platformId;
    private Long inviterId;
    private Long managerId;
    private String tiktokAccount;
    private String username;
    private String fullname;
    private Integer roleId;
    private String regionName;
    private String currencyName;
    private LocalDate startDate;
}
