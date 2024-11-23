package com.rsmanager.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackendUserResetPasswordDTO {
    private Long userId;
    private String oldPassword;
    private String newPassword;
}
